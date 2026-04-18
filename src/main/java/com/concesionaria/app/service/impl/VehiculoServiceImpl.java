package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.InventarioHistorial;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.enumeration.CondicionVehiculo;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.domain.enumeration.EstadoVehiculo;
import com.concesionaria.app.repository.DetalleVentaRepository;
import com.concesionaria.app.repository.InventarioHistorialRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.repository.VersionMotorRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.VehiculoService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.VehiculoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.VehiculoMapper;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final VehiculoMapper vehiculoMapper;
    private final InventarioRepository inventarioRepository;
    private final VentaService ventaService;
    private final VersionMotorRepository versionMotorRepository;
    private final InventarioHistorialRepository inventarioHistorialRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public VehiculoServiceImpl(
        VehiculoRepository vehiculoRepository,
        VehiculoMapper vehiculoMapper,
        InventarioRepository inventarioRepository,
        VentaService ventaService,
        VersionMotorRepository versionMotorRepository,
        InventarioHistorialRepository inventarioHistorialRepository,
        DetalleVentaRepository detalleVentaRepository
    ) {
        this.vehiculoRepository = vehiculoRepository;
        this.vehiculoMapper = vehiculoMapper;
        this.inventarioRepository = inventarioRepository;
        this.ventaService = ventaService;
        this.versionMotorRepository = versionMotorRepository;
        this.inventarioHistorialRepository = inventarioHistorialRepository;
        this.detalleVentaRepository = detalleVentaRepository;
    }

    @Override
    public VehiculoDTO save(VehiculoDTO dto) {
        validarVehiculo(dto, null);
        Instant now = Instant.now();
        String currentUser = currentUserLogin();

        Vehiculo vehiculo = vehiculoMapper.toEntity(dto);
        vehiculo.setCondicion(CondicionVehiculo.EN_VENTA); // compatibilidad temporal
        vehiculo.setCreatedDate(now);
        vehiculo.setCreatedBy(currentUser);
        vehiculo.setLastModifiedDate(now);
        vehiculo.setLastModifiedBy(currentUser);

        Vehiculo saved = vehiculoRepository.save(vehiculo);

        Inventario inventario = new Inventario();
        inventario.setVehiculo(saved);
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setDisponible(true);
        inventario.setFechaIngreso(now);
        inventario.setCreatedDate(now);
        inventario.setCreatedBy(currentUser);
        inventario.setLastModifiedDate(now);
        inventario.setLastModifiedBy(currentUser);
        inventario = inventarioRepository.save(inventario);

        registrarHistorial(inventario, null, EstadoInventario.DISPONIBLE, "ALTA_VEHICULO", "Ingreso inicial desde alta de vehiculo");
        return vehiculoMapper.toDto(saved);
    }

    @Override
    public VehiculoDTO update(VehiculoDTO vehiculoDTO) {
        validarVehiculo(vehiculoDTO, vehiculoDTO.getId());

        Vehiculo existente = vehiculoRepository.findById(vehiculoDTO.getId()).orElseThrow(() -> new BadRequestException("El vehiculo no existe"));
        Vehiculo vehiculo = vehiculoMapper.toEntity(vehiculoDTO);
        vehiculo.setCondicion(existente.getCondicion()); // deprecado: solo espejo de inventario
        vehiculo.setCreatedDate(existente.getCreatedDate());
        vehiculo.setCreatedBy(existente.getCreatedBy());
        vehiculo.setLastModifiedDate(Instant.now());
        vehiculo.setLastModifiedBy(currentUserLogin());

        return vehiculoMapper.toDto(vehiculoRepository.save(vehiculo));
    }

    @Override
    public Optional<VehiculoDTO> partialUpdate(VehiculoDTO vehiculoDTO) {
        return vehiculoRepository
            .findById(vehiculoDTO.getId())
            .map(existente -> {
                if (vehiculoDTO.getPatente() != null) {
                    vehiculoDTO.setPatente(normalizarPatenteNullable(vehiculoDTO.getPatente()));
                }

                vehiculoMapper.partialUpdate(existente, vehiculoDTO);
                validarVehiculo(vehiculoMapper.toDto(existente), existente.getId());
                if (existente.getCreatedBy() == null) {
                    existente.setCreatedBy(currentUserLogin());
                }
                existente.setLastModifiedDate(Instant.now());
                existente.setLastModifiedBy(currentUserLogin());
                return existente;
            })
            .map(vehiculoRepository::save)
            .map(vehiculoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculoDTO> findAll(Pageable pageable) {
        return vehiculoRepository.findAll(pageable).map(vehiculoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoDTO> findAllWhereInventarioIsNull() {
        return vehiculoRepository.findAll().stream().filter(vehiculo -> vehiculo.getInventario() == null).map(vehiculoMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehiculoDTO> findOne(Long id) {
        return vehiculoRepository.findById(id).map(vehiculoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        inventarioRepository.findByVehiculoId(id).ifPresent(inventarioRepository::delete);
        vehiculoRepository.deleteById(id);
    }

    @Override
    public void reservarVehiculo(Long vehiculoId, Long clienteId) {
        throw new BadRequestException("La reserva directa no esta permitida. Inicia una venta y registra la seña minima");
    }

    @Override
    public void venderVehiculo(Long vehiculoId, Long clienteId) {
        ventaService.crearVenta(vehiculoId, clienteId);
    }

    @Override
    public void cancelarReserva(Long vehiculoId) {
        Inventario inventario = inventarioRepository.findByVehiculoId(vehiculoId).orElseThrow(() -> new BadRequestException("Inventario no encontrado"));
        if (inventario.getEstadoInventario() != EstadoInventario.RESERVADO) {
            throw new BadRequestException("El vehiculo no tiene una reserva activa");
        }

        EstadoInventario anterior = inventario.getEstadoInventario();
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setDisponible(true);
        inventario.setClienteReserva(null);
        inventario.setFechaReserva(null);
        inventario.setFechaVencimientoReserva(null);
        inventario.setLastModifiedDate(Instant.now());
        inventario.setLastModifiedBy(currentUserLogin());
        inventarioRepository.save(inventario);

        syncVehiculo(inventario);
        registrarHistorial(inventario, anterior, EstadoInventario.DISPONIBLE, "CANCELACION_RESERVA", "Liberacion de reserva activa");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehiculoDTO> findByPatente(String patente) {
        String patenteNormalizada = normalizarPatenteNullable(patente);
        if (patenteNormalizada == null) {
            return Optional.empty();
        }
        return vehiculoRepository.findByPatente(patenteNormalizada).map(vehiculoMapper::toDto);
    }

    private void syncVehiculo(Inventario inventario) {
        Vehiculo vehiculo = inventario.getVehiculo();
        if (vehiculo == null) {
            return;
        }

        switch (inventario.getEstadoInventario()) {
            case DISPONIBLE -> vehiculo.setCondicion(CondicionVehiculo.EN_VENTA);
            case RESERVADO -> vehiculo.setCondicion(CondicionVehiculo.RESERVADO);
            case VENDIDO -> vehiculo.setCondicion(CondicionVehiculo.VENDIDO);
        }
        vehiculo.setLastModifiedDate(Instant.now());
        vehiculo.setLastModifiedBy(currentUserLogin());
        vehiculoRepository.save(vehiculo);
    }

    private void validarVehiculo(VehiculoDTO vehiculoDTO, Long idActual) {
        if (vehiculoDTO == null) {
            throw new BadRequestException("El vehiculo es obligatorio");
        }

        if (idActual != null) {
            inventarioRepository.findByVehiculoId(idActual).ifPresent(inventario -> {
                if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
                    throw new BadRequestException("No se puede editar un vehiculo ya vendido");
                }
            });
        }

        String patenteNormalizada = normalizarPatenteNullable(vehiculoDTO.getPatente());
        vehiculoDTO.setPatente(patenteNormalizada);

        if (EstadoVehiculo.USADO.equals(vehiculoDTO.getEstado()) && patenteNormalizada == null) {
            throw new BadRequestException("La patente es obligatoria para vehiculos usados");
        }

        if (patenteNormalizada != null) {
            vehiculoRepository
                .findByPatenteIgnoreCase(patenteNormalizada)
                .filter(vehiculo -> !Objects.equals(vehiculo.getId(), idActual))
                .ifPresent(vehiculo -> {
                    throw new BadRequestException("Ya existe un vehiculo con esa patente");
                });
        }

        if (
            vehiculoDTO.getVersion() != null &&
            vehiculoDTO.getVersion().getId() != null &&
            vehiculoDTO.getMotor() != null &&
            vehiculoDTO.getMotor().getId() != null &&
            !versionMotorRepository.existsByVersionIdAndMotorId(vehiculoDTO.getVersion().getId(), vehiculoDTO.getMotor().getId())
        ) {
            throw new BadRequestException("El motor seleccionado no es compatible con la version elegida");
        }
    }

    private String normalizarPatenteNullable(String patente) {
        if (patente == null || patente.trim().isEmpty()) {
            return null;
        }
        String patenteNormalizada = patente.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return patenteNormalizada.isEmpty() ? null : patenteNormalizada;
    }

    private void registrarHistorial(
        Inventario inventario,
        EstadoInventario estadoAnterior,
        EstadoInventario estadoNuevo,
        String accion,
        String detalle
    ) {
        InventarioHistorial historial = new InventarioHistorial();
        historial.setInventario(inventario);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setAccion(accion);
        historial.setDetalle(detalle);
        historial.setFecha(Instant.now());
        inventarioHistorialRepository.save(historial);
    }

    private void liberarReservaSiVencida(Inventario inventario) {
        if (inventario.getEstadoInventario() != EstadoInventario.RESERVADO) {
            return;
        }
        Instant vencimiento = inventario.getFechaVencimientoReserva();
        if (vencimiento == null || vencimiento.isAfter(Instant.now())) {
            return;
        }
        if (inventario.getVehiculo() != null && inventario.getVehiculo().getId() != null) {
            boolean ventaActiva = detalleVentaRepository.existsByVehiculoIdAndVentaEstadoIn(
                inventario.getVehiculo().getId(),
                EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA, EstadoVenta.FINALIZADA)
            );
            if (ventaActiva) {
                return;
            }
        }

        EstadoInventario anterior = inventario.getEstadoInventario();
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setDisponible(true);
        inventario.setClienteReserva(null);
        inventario.setFechaReserva(null);
        inventario.setFechaVencimientoReserva(null);
        inventario.setLastModifiedDate(Instant.now());
        inventario.setLastModifiedBy(currentUserLogin());
        inventarioRepository.save(inventario);

        syncVehiculo(inventario);
        registrarHistorial(inventario, anterior, EstadoInventario.DISPONIBLE, "RESERVA_EXPIRADA", "Reserva vencida automaticamente");
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }

    private Instant plusOneMonth(Instant base) {
        return base.atZone(ZoneOffset.UTC).plusMonths(1).toInstant();
    }
}
