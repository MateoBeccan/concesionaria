package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.InventarioHistorial;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.enumeration.CondicionVehiculo;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.repository.DetalleVentaRepository;
import com.concesionaria.app.repository.InventarioHistorialRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.InventarioService;
import com.concesionaria.app.service.dto.InventarioDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.InventarioMapper;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private static final Logger LOG = LoggerFactory.getLogger(InventarioServiceImpl.class);

    private final InventarioRepository inventarioRepository;
    private final InventarioMapper inventarioMapper;
    private final VehiculoRepository vehiculoRepository;
    private final ClienteRepository clienteRepository;
    private final InventarioHistorialRepository inventarioHistorialRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public InventarioServiceImpl(
        InventarioRepository inventarioRepository,
        InventarioMapper inventarioMapper,
        VehiculoRepository vehiculoRepository,
        ClienteRepository clienteRepository,
        InventarioHistorialRepository inventarioHistorialRepository,
        DetalleVentaRepository detalleVentaRepository
    ) {
        this.inventarioRepository = inventarioRepository;
        this.inventarioMapper = inventarioMapper;
        this.vehiculoRepository = vehiculoRepository;
        this.clienteRepository = clienteRepository;
        this.inventarioHistorialRepository = inventarioHistorialRepository;
        this.detalleVentaRepository = detalleVentaRepository;
    }

    @Override
    public InventarioDTO save(InventarioDTO dto) {
        Inventario inventario = buildValidatedEntity(dto, null);
        Instant now = Instant.now();
        String currentUser = currentUserLogin();
        inventario.setCreatedDate(now);
        inventario.setCreatedBy(currentUser);
        inventario.setLastModifiedDate(now);
        inventario.setLastModifiedBy(currentUser);
        Inventario saved = inventarioRepository.save(inventario);
        registrarCambioEstado(saved, null, "ALTA_INVENTARIO", "Ingreso inicial de unidad al inventario");
        syncVehiculoCondicion(saved);
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
        Inventario saved = inventarioRepository.save(inventario);
        registrarCambioEstado(saved, estadoAnterior, "ACTUALIZACION_INVENTARIO", "Actualizacion manual del inventario");
        syncVehiculoCondicion(saved);
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
                validarYNormalizar(existente, dto.getId(), existente.getVehiculo(), existente.getClienteReserva());
                if (existente.getCreatedBy() == null) {
                    existente.setCreatedBy(currentUserLogin());
                }
                existente.setLastModifiedDate(Instant.now());
                existente.setLastModifiedBy(currentUserLogin());
                Inventario saved = inventarioRepository.save(existente);
                registrarCambioEstado(saved, estadoAnterior, "ACTUALIZACION_PARCIAL_INVENTARIO", "Actualizacion parcial de inventario");
                syncVehiculoCondicion(saved);
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
        Instant now = Instant.now();
        List<Inventario> vencidas = inventarioRepository.findAllByEstadoInventarioAndFechaVencimientoReservaBefore(EstadoInventario.RESERVADO, now);
        long expiradas = 0L;

        for (Inventario inventario : vencidas) {
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
        Cliente clienteReserva = resolveClienteReserva(dto);

        Inventario inventario = existente == null ? new Inventario() : existente;
        inventario.setFechaIngreso(dto.getFechaIngreso());
        inventario.setUbicacion(dto.getUbicacion());
        inventario.setEstadoInventario(dto.getEstadoInventario());
        inventario.setDisponible(dto.getDisponible());
        inventario.setObservaciones(dto.getObservaciones());
        inventario.setFechaReserva(dto.getFechaReserva());
        inventario.setFechaVencimientoReserva(dto.getFechaVencimientoReserva());
        inventario.setVehiculo(vehiculo);
        inventario.setClienteReserva(clienteReserva);

        validarYNormalizar(inventario, existente != null ? existente.getId() : null, vehiculo, clienteReserva);

        return inventario;
    }

    private Vehiculo resolveVehiculo(InventarioDTO dto) {
        Long vehiculoId = dto.getVehiculo() != null ? dto.getVehiculo().getId() : null;
        if (vehiculoId == null) {
            throw new BadRequestException("El vehiculo es obligatorio para registrar inventario");
        }
        return vehiculoRepository.findById(vehiculoId).orElseThrow(() -> new BadRequestException("El vehiculo seleccionado no existe"));
    }

    private Cliente resolveClienteReserva(InventarioDTO dto) {
        Long clienteReservaId = dto.getClienteReserva() != null ? dto.getClienteReserva().getId() : null;
        if (clienteReservaId == null) {
            return null;
        }
        return clienteRepository.findById(clienteReservaId).orElseThrow(() -> new BadRequestException("El cliente de reserva no existe"));
    }

    private void validarYNormalizar(Inventario inventario, Long inventarioId, Vehiculo vehiculo, Cliente clienteReserva) {
        if (vehiculo == null) {
            throw new BadRequestException("El inventario debe estar asociado a un vehiculo");
        }

        inventarioRepository
            .findByVehiculoId(vehiculo.getId())
            .filter(existing -> !Objects.equals(existing.getId(), inventarioId))
            .ifPresent(existing -> {
                throw new BadRequestException("El vehiculo ya tiene un inventario asociado");
            });

        Inventario existingForVehicle = inventarioId != null ? inventarioRepository.findById(inventarioId).orElse(null) : null;
        if (
            existingForVehicle != null &&
            existingForVehicle.getEstadoInventario() == EstadoInventario.RESERVADO &&
            inventario.getEstadoInventario() == EstadoInventario.RESERVADO &&
            existingForVehicle.getClienteReserva() != null &&
            !Objects.equals(existingForVehicle.getClienteReserva().getId(), clienteReserva != null ? clienteReserva.getId() : null)
        ) {
            throw new BadRequestException("Libera la reserva actual antes de asignarla a otro cliente");
        }

        switch (inventario.getEstadoInventario()) {
            case DISPONIBLE -> validarDisponible(inventario);
            case RESERVADO -> validarReservado(inventario, clienteReserva, existingForVehicle);
            case VENDIDO -> validarVendido(inventario);
            default -> throw new BadRequestException("El estado de inventario es obligatorio");
        }

    }

    private void validarDisponible(Inventario inventario) {
        inventario.setDisponible(true);
        inventario.setClienteReserva(null);
        inventario.setFechaReserva(null);
        inventario.setFechaVencimientoReserva(null);
    }

    private void validarReservado(Inventario inventario, Cliente clienteReserva, Inventario existente) {
        if (existente == null || existente.getEstadoInventario() != EstadoInventario.RESERVADO) {
            throw new BadRequestException("La reserva se gestiona exclusivamente desde venta con seña minima");
        }
        if (clienteReserva == null) {
            throw new BadRequestException("Debes seleccionar un cliente para registrar la reserva");
        }
        if (inventario.getFechaReserva() == null) {
            inventario.setFechaReserva(Instant.now());
        }
        if (inventario.getFechaVencimientoReserva() == null) {
            inventario.setFechaVencimientoReserva(plusOneMonth(inventario.getFechaReserva()));
        }
        if (!inventario.getFechaVencimientoReserva().isAfter(inventario.getFechaReserva())) {
            throw new BadRequestException("El vencimiento de la reserva debe ser posterior a la fecha de reserva");
        }
        inventario.setDisponible(false);
    }

    private void validarVendido(Inventario inventario) {
        inventario.setDisponible(false);
        inventario.setClienteReserva(null);
        inventario.setFechaReserva(null);
        inventario.setFechaVencimientoReserva(null);
    }

    private void syncVehiculoCondicion(Inventario inventario) {
        Vehiculo vehiculo = inventario.getVehiculo();
        if (vehiculo == null) {
            return;
        }

        CondicionVehiculo condicion = switch (inventario.getEstadoInventario()) {
            case DISPONIBLE -> CondicionVehiculo.EN_VENTA;
            case RESERVADO -> CondicionVehiculo.RESERVADO;
            case VENDIDO -> CondicionVehiculo.VENDIDO;
        };

        if (vehiculo.getCondicion() != condicion) {
            vehiculo.setCondicion(condicion);
            vehiculo.setLastModifiedDate(Instant.now());
            vehiculo.setLastModifiedBy(currentUserLogin());
            vehiculoRepository.save(vehiculo);
            LOG.debug("Vehiculo {} sincronizado por compatibilidad a condicion {}", vehiculo.getId(), condicion);
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
        historial.setFecha(Instant.now());
        inventarioHistorialRepository.save(historial);
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }

    private boolean expirarReservaVencidaSiCorresponde(Inventario inventario) {
        if (inventario == null || inventario.getEstadoInventario() != EstadoInventario.RESERVADO) {
            return false;
        }

        Instant vencimiento = inventario.getFechaVencimientoReserva();
        if (vencimiento == null || !vencimiento.isBefore(Instant.now())) {
            return false;
        }

        if (inventario.getVehiculo() != null && inventario.getVehiculo().getId() != null) {
            boolean ventaActiva = detalleVentaRepository.existsByVehiculoIdAndVentaEstadoIn(
                inventario.getVehiculo().getId(),
                EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA, EstadoVenta.FINALIZADA)
            );
            if (ventaActiva) {
                return false;
            }
        }

        EstadoInventario estadoAnterior = inventario.getEstadoInventario();
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setDisponible(true);
        inventario.setClienteReserva(null);
        inventario.setFechaReserva(null);
        inventario.setFechaVencimientoReserva(null);
        inventario.setLastModifiedDate(Instant.now());
        inventario.setLastModifiedBy(currentUserLogin());

        Inventario saved = inventarioRepository.save(inventario);
        syncVehiculoCondicion(saved);
        registrarCambioEstado(saved, estadoAnterior, "RESERVA_EXPIRADA", "Reserva vencida automaticamente");
        return true;
    }

    private Instant plusOneMonth(Instant base) {
        return base.atZone(ZoneOffset.UTC).plusMonths(1).toInstant();
    }
}
