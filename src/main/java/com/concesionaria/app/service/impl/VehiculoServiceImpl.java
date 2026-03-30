package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.*;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVehiculo;
import com.concesionaria.app.repository.*;
import com.concesionaria.app.service.VehiculoService;
import com.concesionaria.app.service.dto.VehiculoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.VehiculoMapper;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.concesionaria.app.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VehiculoServiceImpl implements VehiculoService {

    private static final Logger LOG = LoggerFactory.getLogger(VehiculoServiceImpl.class);

    private final VehiculoRepository vehiculoRepository;
    private final VehiculoMapper vehiculoMapper;
    private final InventarioRepository inventarioRepository;
    private final ClienteRepository clienteRepository;
    private final VersionRepository versionRepository;
    private final MotorRepository motorRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;

    public VehiculoServiceImpl(
        VehiculoRepository vehiculoRepository,
        VehiculoMapper vehiculoMapper,
        InventarioRepository inventarioRepository, ClienteRepository clienteRepository,
        VersionRepository versionRepository, MotorRepository motorRepository,
        TipoVehiculoRepository tipoVehiculoRepository)
     {
        this.vehiculoRepository = vehiculoRepository;
        this.vehiculoMapper = vehiculoMapper;
        this.inventarioRepository = inventarioRepository;
        this.clienteRepository = clienteRepository;
        this.versionRepository = versionRepository;
        this.motorRepository = motorRepository;
        this.tipoVehiculoRepository = tipoVehiculoRepository;
    }

    // =========================
    // CREATE
    // =========================
    @Override
    public VehiculoDTO save(VehiculoDTO vehiculoDTO) {

        LOG.info("Creando vehiculo {}", vehiculoDTO.getPatente());

        Vehiculo vehiculo = vehiculoMapper.toEntity(vehiculoDTO);

        // VALIDACIONES
        validarVehiculo(vehiculo, true);

        vehiculo.setCreatedDate(Instant.now());

        Vehiculo saved = vehiculoRepository.save(vehiculo);

        // CREAR INVENTARIO AUTOMÁTICO
        Inventario inv = new Inventario();
        inv.setVehiculo(saved);
        inv.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inv.setDisponible(true);
        inv.setFechaIngreso(Instant.now());

        inventarioRepository.save(inv);

        return vehiculoMapper.toDto(saved);
    }

    // =========================
    // UPDATE
    // =========================
    @Override
    public VehiculoDTO update(VehiculoDTO vehiculoDTO) {

        LOG.info("Actualizando vehiculo {}", vehiculoDTO.getId());

        Vehiculo vehiculo = vehiculoMapper.toEntity(vehiculoDTO);

        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoDTO.getId())
            .orElseThrow(() -> new BadRequestAlertException("No existe inventario", "vehiculo", "inventarionotfound"));

        if (inv.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new BadRequestAlertException("No se puede modificar un vehiculo vendido", "vehiculo", "vehiculovendido");
        }

        validarVehiculo(vehiculo, false);

        vehiculo.setLastModifiedDate(Instant.now());

        return vehiculoMapper.toDto(vehiculoRepository.save(vehiculo));
    }

    // =========================
    // PARTIAL UPDATE
    // =========================
    @Override
    public Optional<VehiculoDTO> partialUpdate(VehiculoDTO vehiculoDTO) {

        return vehiculoRepository
            .findById(vehiculoDTO.getId())
            .map(existing -> {
                vehiculoMapper.partialUpdate(existing, vehiculoDTO);
                existing.setLastModifiedDate(Instant.now());
                return existing;
            })
            .map(vehiculoRepository::save)
            .map(vehiculoMapper::toDto);
    }

    // =========================
    // GET
    // =========================
    @Override
    @Transactional(readOnly = true)
    public Page<VehiculoDTO> findAll(Pageable pageable) {
        return vehiculoRepository.findAll(pageable).map(vehiculoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoDTO> findAllWhereInventarioIsNull() {
        return StreamSupport.stream(vehiculoRepository.findAll().spliterator(), false)
            .filter(v -> v.getInventario() == null)
            .map(vehiculoMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehiculoDTO> findOne(Long id) {
        return vehiculoRepository.findById(id).map(vehiculoMapper::toDto);
    }

    // =========================
    // DELETE
    // =========================
    @Override
    public void delete(Long id) {

        Inventario inv = inventarioRepository.findByVehiculoId(id)
            .orElseThrow(() -> new BadRequestAlertException("No existe inventario", "vehiculo", "inventarionotfound"));

        if (inv.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new BadRequestAlertException("No se puede eliminar un vehiculo vendido", "vehiculo", "vehiculovendido");
        }

        vehiculoRepository.deleteById(id);
    }

    // =========================
    // VALIDACIONES CENTRALIZADAS
    // =========================
    private void validarVehiculo(Vehiculo vehiculo, boolean isCreate) {

        // ======================
        // DATOS BÁSICOS
        // ======================

        if (vehiculo.getPatente() == null || vehiculo.getPatente().isBlank()) {
            throw new BadRequestException("La patente es obligatoria");
        }

        vehiculo.setPatente(vehiculo.getPatente().trim().toUpperCase());

        if (vehiculo.getPrecio() == null || vehiculo.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Precio inválido");
        }

        if (vehiculo.getFechaFabricacion() == null ||
            vehiculo.getFechaFabricacion().isAfter(LocalDate.now())) {
            throw new BadRequestException("Fecha de fabricación inválida");
        }

        // ======================
        // RELACIONES
        // ======================

        if (vehiculo.getVersion() == null || vehiculo.getVersion().getId() == null) {
            throw new BadRequestException("Debe especificar una versión");
        }

        Version version = versionRepository.findById(vehiculo.getVersion().getId())
            .orElseThrow(() -> new BadRequestException("La versión no existe"));

        if (version.getModelo() == null) {
            throw new BadRequestException("La versión no tiene modelo asociado");
        }

        if (vehiculo.getMotor() == null || vehiculo.getMotor().getId() == null) {
            throw new BadRequestException("Debe especificar un motor");
        }

        if (!motorRepository.existsById(vehiculo.getMotor().getId())) {
            throw new BadRequestException("El motor no existe");
        }

        if (vehiculo.getTipoVehiculo() == null || vehiculo.getTipoVehiculo().getId() == null) {
            throw new BadRequestException("Debe especificar el tipo de vehículo");
        }

        if (!tipoVehiculoRepository.existsById(vehiculo.getTipoVehiculo().getId())) {
            throw new BadRequestException("El tipo de vehículo no existe");
        }

        // ======================
        // REGLAS DE NEGOCIO
        // ======================

        if (vehiculo.getEstado() == EstadoVehiculo.NUEVO && vehiculo.getKm() != 0) {
            throw new BadRequestException("Un vehículo nuevo debe tener 0 km");
        }

        if (vehiculo.getEstado() == EstadoVehiculo.USADO && vehiculo.getKm() <= 0) {
            throw new BadRequestException("Un vehículo usado debe tener km mayor a 0");
        }

        // ======================
        // UNICIDAD
        // ======================

        Optional<Vehiculo> existente = vehiculoRepository.findByPatente(vehiculo.getPatente());

        if (existente.isPresent() &&
            (isCreate || !existente.get().getId().equals(vehiculo.getId()))) {

            throw new BadRequestException("Ya existe un vehículo con esa patente");
        }
    }

    @Override
    public void reservarVehiculo(Long vehiculoId, Long clienteId) {

        LOG.info("Reservando vehiculo {} para cliente {}", vehiculoId, clienteId);

        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoId)
            .orElseThrow(() -> new BadRequestAlertException("No existe inventario", "vehiculo", "inventarionotfound"));

        if (inv.getEstadoInventario() != EstadoInventario.DISPONIBLE) {
            throw new BadRequestAlertException("Vehiculo no disponible para reservar", "vehiculo", "nodisponible");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new BadRequestAlertException("Cliente no existe", "cliente", "notfound"));

        inv.setEstadoInventario(EstadoInventario.RESERVADO);
        inv.setDisponible(false);
        inv.setClienteReserva(cliente);
        inv.setFechaReserva(Instant.now());

        // opcional: vencimiento 3 días
        inv.setFechaVencimientoReserva(Instant.now().plus(3, java.time.temporal.ChronoUnit.DAYS));

        inventarioRepository.save(inv);
    }

    @Override
    public void venderVehiculo(Long vehiculoId, Long clienteId) {

        LOG.info("Vendiendo vehiculo {} al cliente {}", vehiculoId, clienteId);

        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoId)
            .orElseThrow(() -> new BadRequestAlertException("No existe inventario", "vehiculo", "inventarionotfound"));

        if (inv.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new BadRequestAlertException("Vehiculo ya vendido", "vehiculo", "vendido");
        }

        // 🔥 CASO RESERVADO
        if (inv.getEstadoInventario() == EstadoInventario.RESERVADO) {

            if (inv.getClienteReserva() == null) {
                throw new BadRequestAlertException("Reserva inconsistente", "vehiculo", "error");
            }

            if (!inv.getClienteReserva().getId().equals(clienteId)) {
                throw new BadRequestAlertException(
                    "Solo el cliente que reservó puede comprar este vehiculo",
                    "vehiculo",
                    "clienteinvalido"
                );
            }
        }

        // CASO DISPONIBLE → venta directa OK

        inv.setEstadoInventario(EstadoInventario.VENDIDO);
        inv.setDisponible(false);
        inv.setClienteReserva(null);
        inv.setFechaReserva(null);
        inv.setFechaVencimientoReserva(null);

        inventarioRepository.save(inv);
    }

    @Override
    public void cancelarReserva(Long vehiculoId) {

        LOG.info("Cancelando reserva del vehiculo {}", vehiculoId);

        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoId)
            .orElseThrow(() -> new BadRequestAlertException("No existe inventario", "vehiculo", "inventarionotfound"));

        if (inv.getEstadoInventario() != EstadoInventario.RESERVADO) {
            throw new BadRequestAlertException("El vehiculo no está reservado", "vehiculo", "noreservado");
        }

        inv.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inv.setDisponible(true);
        inv.setClienteReserva(null);
        inv.setFechaReserva(null);
        inv.setFechaVencimientoReserva(null);

        inventarioRepository.save(inv);
    }
}
