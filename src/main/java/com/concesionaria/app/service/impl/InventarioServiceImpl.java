package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.InventarioHistorial;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.UbicacionStock;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.InventarioHistorialRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.UbicacionStockRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.InventarioService;
import com.concesionaria.app.service.dto.InventarioDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.InventarioMapper;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final InventarioMapper inventarioMapper;
    private final VehiculoRepository vehiculoRepository;
    private final ReservaRepository reservaRepository;
    private final InventarioHistorialRepository inventarioHistorialRepository;
    private final VentaRepository ventaRepository;
    private final UbicacionStockRepository ubicacionStockRepository;
    private final LegacySchemaCompatibilityService legacySchemaCompatibilityService;

    public InventarioServiceImpl(
        InventarioRepository inventarioRepository,
        InventarioMapper inventarioMapper,
        VehiculoRepository vehiculoRepository,
        ReservaRepository reservaRepository,
        InventarioHistorialRepository inventarioHistorialRepository,
        VentaRepository ventaRepository,
        UbicacionStockRepository ubicacionStockRepository,
        LegacySchemaCompatibilityService legacySchemaCompatibilityService
    ) {
        this.inventarioRepository = inventarioRepository;
        this.inventarioMapper = inventarioMapper;
        this.vehiculoRepository = vehiculoRepository;
        this.reservaRepository = reservaRepository;
        this.inventarioHistorialRepository = inventarioHistorialRepository;
        this.ventaRepository = ventaRepository;
        this.ubicacionStockRepository = ubicacionStockRepository;
        this.legacySchemaCompatibilityService = legacySchemaCompatibilityService;
    }

    @Override
    public InventarioDTO save(InventarioDTO dto) {
        Inventario inventario = buildValidatedEntity(dto, null);
        Instant now = Instant.now();
        String currentUser = currentUserLogin();
        normalizarCodigoInternoStock(inventario, inventario.getVehiculo());
        inventario.setCreatedDate(now);
        inventario.setCreatedBy(currentUser);
        inventario.setLastModifiedDate(now);
        inventario.setLastModifiedBy(currentUser);
        legacySchemaCompatibilityService.ensureInventarioDisponibleDefault();
        Inventario saved = inventarioRepository.save(inventario);
        registrarCambioEstado(saved, null, "ALTA_INVENTARIO", "Ingreso inicial de unidad al inventario");
        return inventarioMapper.toDto(saved);
    }

    @Override
    public InventarioDTO update(InventarioDTO dto) {
        Inventario existente = inventarioRepository.findById(dto.getId()).orElseThrow(() -> new BadRequestException("El inventario no existe"));
        expirarReservaVencidaSiCorresponde(existente);
        EstadoInventario estadoAnterior = existente.getEstadoInventario();

        Inventario inventario = buildValidatedEntity(dto, existente);
        inventario.setCreatedDate(existente.getCreatedDate());
        inventario.setCreatedBy(existente.getCreatedBy());
        inventario.setLastModifiedDate(Instant.now());
        inventario.setLastModifiedBy(currentUserLogin());
        legacySchemaCompatibilityService.ensureInventarioDisponibleDefault();
        Inventario saved = inventarioRepository.save(inventario);
        registrarCambioEstado(saved, estadoAnterior, "ACTUALIZACION_INVENTARIO", "Actualizacion manual del inventario");
        return inventarioMapper.toDto(saved);
    }

    @Override
    public Optional<InventarioDTO> partialUpdate(InventarioDTO dto) {
        return inventarioRepository
            .findById(dto.getId())
            .map(existente -> {
                expirarReservaVencidaSiCorresponde(existente);
                EstadoInventario estadoAnterior = existente.getEstadoInventario();
                inventarioMapper.partialUpdate(existente, dto);
                validarYNormalizar(existente, dto.getId(), existente.getVehiculo());
                if (existente.getCreatedBy() == null) {
                    existente.setCreatedBy(currentUserLogin());
                }
                existente.setLastModifiedDate(Instant.now());
                existente.setLastModifiedBy(currentUserLogin());
                legacySchemaCompatibilityService.ensureInventarioDisponibleDefault();
                Inventario saved = inventarioRepository.save(existente);
                registrarCambioEstado(saved, estadoAnterior, "ACTUALIZACION_PARCIAL_INVENTARIO", "Actualizacion parcial de inventario");
                return inventarioMapper.toDto(saved);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioDTO> findAll(Pageable pageable) {
        return inventarioRepository.findAll(pageable).map(inventarioMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventarioDTO> findOne(Long id) {
        return inventarioRepository.findById(id).map(inventarioMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventarioDTO> findByVehiculoId(Long vehiculoId) {
        if (vehiculoId == null) {
            return Optional.empty();
        }
        return inventarioRepository.findByVehiculoId(vehiculoId).map(inventarioMapper::toDto);
    }

    @Override
    public long expirarReservasVencidas() {
        long expiradas = 0L;
        for (Reserva reserva : reservaRepository.findAllByEstadoAndFechaVencimientoBefore(EstadoReserva.ACTIVA, Instant.now())) {
            Inventario inventario = reserva.getInventario();
            if (inventario == null || inventario.getId() == null || inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
                continue;
            }
            if (!expirarReservaVencidaSiCorresponde(inventario)) {
                continue;
            }
            expiradas++;
        }
        return expiradas;
    }

    @Override
    public void delete(Long id) {
        inventarioRepository.deleteById(id);
    }

    private Inventario buildValidatedEntity(InventarioDTO dto, Inventario existente) {
        if (dto == null) {
            throw new BadRequestException("El inventario es obligatorio");
        }

        Vehiculo vehiculo = resolveVehiculo(dto);
        Inventario inventario = existente == null ? new Inventario() : existente;
        inventario.setFechaIngreso(dto.getFechaIngreso());
        inventario.setCodigoInternoStock(dto.getCodigoInternoStock());
        inventario.setCostoAdquisicion(dto.getCostoAdquisicion());
        inventario.setFechaEgreso(dto.getFechaEgreso());
        inventario.setTipoOrigenIngreso(dto.getTipoOrigenIngreso());
        inventario.setOrigenVehiculo(dto.getOrigenVehiculo());
        inventario.setTipoTenencia(dto.getTipoTenencia());
        inventario.setEstadoOperativoDocumental(dto.getEstadoOperativoDocumental());
        inventario.setProveedorReferencia(dto.getProveedorReferencia());
        inventario.setNumeroInternoStock(dto.getNumeroInternoStock());
        inventario.setUbicacion(dto.getUbicacion());
        inventario.setUbicacionStock(resolveUbicacionStock(dto));
        inventario.setEstadoInventario(dto.getEstadoInventario());
        inventario.setObservaciones(dto.getObservaciones());
        inventario.setVehiculo(vehiculo);

        validarYNormalizar(inventario, existente != null ? existente.getId() : null, vehiculo);

        return inventario;
    }

    private Vehiculo resolveVehiculo(InventarioDTO dto) {
        Long vehiculoId = dto.getVehiculo() != null ? dto.getVehiculo().getId() : null;
        if (vehiculoId == null) {
            throw new BadRequestException("El vehiculo es obligatorio para registrar inventario");
        }
        return vehiculoRepository.findById(vehiculoId).orElseThrow(() -> new BadRequestException("El vehiculo seleccionado no existe"));
    }

    private UbicacionStock resolveUbicacionStock(InventarioDTO dto) {
        Long ubicacionId = dto.getUbicacionStock() != null ? dto.getUbicacionStock().getId() : null;
        if (ubicacionId == null) {
            return null;
        }
        return ubicacionStockRepository.findById(ubicacionId).orElseThrow(() -> new BadRequestException("La ubicacion de stock no existe"));
    }

    private void validarYNormalizar(Inventario inventario, Long inventarioId, Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new BadRequestException("El inventario debe estar asociado a un vehiculo");
        }
        normalizarCodigoInternoStock(inventario, vehiculo);
        if (inventario.getNumeroInternoStock() == null || inventario.getNumeroInternoStock().isBlank()) {
            inventario.setNumeroInternoStock(inventario.getCodigoInternoStock());
        }

        inventarioRepository
            .findByVehiculoId(vehiculo.getId())
            .filter(existing -> !Objects.equals(existing.getId(), inventarioId))
            .ifPresent(existing -> {
                throw new BadRequestException("El vehiculo ya tiene un inventario asociado");
            });

        if (inventario.getEstadoInventario() == null) {
            throw new BadRequestException("El estado de inventario es obligatorio");
        }
        if (inventario.getEstadoInventario() == EstadoInventario.RESERVADO) {
            throw new BadRequestException("Las reservas se gestionan exclusivamente desde la entidad Reserva");
        }
        if (inventario.getEstadoInventario() == EstadoInventario.DISPONIBLE) {
            inventario.setFechaEgreso(null);
        } else if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO && inventario.getFechaEgreso() == null) {
            inventario.setFechaEgreso(Instant.now());
        }
    }

    private void registrarCambioEstado(Inventario inventario, EstadoInventario estadoAnterior, String accion, String detalle) {
        if (inventario == null || inventario.getEstadoInventario() == null) {
            return;
        }
        if (estadoAnterior != null && estadoAnterior == inventario.getEstadoInventario()) {
            return;
        }

        InventarioHistorial historial = new InventarioHistorial();
        historial.setInventario(inventario);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(inventario.getEstadoInventario());
        historial.setAccion(accion);
        historial.setDetalle(detalle);
        historial.setMotivo(detalle);
        reservaRepository
            .findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(inventario.getId(), EstadoReserva.ACTIVA)
            .map(Reserva::getCliente)
            .ifPresent(historial::setCliente);
        historial.setFecha(Instant.now());
        historial.setUsuario(currentUserLogin());
        inventarioHistorialRepository.save(historial);
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }

    private String generarCodigoInternoStock(Vehiculo vehiculo) {
        String sufijo = vehiculo != null && vehiculo.getId() != null ? vehiculo.getId().toString() : String.valueOf(System.currentTimeMillis());
        return ("INV-" + sufijo).toUpperCase();
    }

    private void normalizarCodigoInternoStock(Inventario inventario, Vehiculo vehiculo) {
        if (inventario.getCodigoInternoStock() == null || inventario.getCodigoInternoStock().isBlank()) {
            inventario.setCodigoInternoStock(generarCodigoInternoStock(vehiculo));
            return;
        }
        inventario.setCodigoInternoStock(inventario.getCodigoInternoStock().trim().toUpperCase());
    }

    private boolean expirarReservaVencidaSiCorresponde(Inventario inventario) {
        if (inventario == null || inventario.getId() == null || inventario.getEstadoInventario() != EstadoInventario.RESERVADO) {
            return false;
        }

        Optional<Reserva> reservaActivaOpt = reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(
            inventario.getId(),
            EstadoReserva.ACTIVA
        );
        if (reservaActivaOpt.isEmpty()) {
            return false;
        }

        Reserva reservaActiva = reservaActivaOpt.get();
        Instant vencimiento = reservaActiva.getFechaVencimiento();
        if (vencimiento == null || !vencimiento.isBefore(Instant.now())) {
            return false;
        }

        if (inventario.getVehiculo() != null && inventario.getVehiculo().getId() != null) {
            boolean ventaActiva = ventaRepository.existsByVehiculoIdAndEstadoIn(
                inventario.getVehiculo().getId(),
                EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA, EstadoVenta.FINALIZADA)
            );
            if (ventaActiva) {
                return false;
            }
        }

        EstadoInventario estadoAnterior = inventario.getEstadoInventario();
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setLastModifiedDate(Instant.now());
        inventario.setLastModifiedBy(currentUserLogin());

        reservaActiva.setEstado(EstadoReserva.VENCIDA);
        reservaActiva.setLastModifiedDate(Instant.now());
        reservaRepository.save(reservaActiva);

        Inventario saved = inventarioRepository.save(inventario);
        registrarCambioEstado(saved, estadoAnterior, "RESERVA_EXPIRADA", "Reserva vencida automaticamente");
        return true;
    }
}
