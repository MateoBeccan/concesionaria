package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.enumeration.CondicionVehiculo;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVehiculo;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.repository.VersionMotorRepository;
import com.concesionaria.app.service.VehiculoService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.VehiculoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.VehiculoMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final ClienteRepository clienteRepository;
    private final VentaService ventaService;
    private final VersionMotorRepository versionMotorRepository;

    public VehiculoServiceImpl(
        VehiculoRepository vehiculoRepository,
        VehiculoMapper vehiculoMapper,
        InventarioRepository inventarioRepository,
        ClienteRepository clienteRepository,
        VentaService ventaService,
        VersionMotorRepository versionMotorRepository
    ) {
        this.vehiculoRepository = vehiculoRepository;
        this.vehiculoMapper = vehiculoMapper;
        this.inventarioRepository = inventarioRepository;
        this.clienteRepository = clienteRepository;
        this.ventaService = ventaService;
        this.versionMotorRepository = versionMotorRepository;
    }

    @Override
    public VehiculoDTO save(VehiculoDTO dto) {
        validarVehiculo(dto, null);

        Vehiculo vehiculo = vehiculoMapper.toEntity(dto);
        vehiculo.setCreatedDate(Instant.now());
        vehiculo.setLastModifiedDate(Instant.now());

        Vehiculo saved = vehiculoRepository.save(vehiculo);

        Inventario inventario = new Inventario();
        inventario.setVehiculo(saved);
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setDisponible(true);
        inventario.setFechaIngreso(Instant.now());
        inventarioRepository.save(inventario);

        return vehiculoMapper.toDto(saved);
    }

    @Override
    public VehiculoDTO update(VehiculoDTO vehiculoDTO) {
        validarVehiculo(vehiculoDTO, vehiculoDTO.getId());

        Vehiculo existente = vehiculoRepository.findById(vehiculoDTO.getId()).orElseThrow(() -> new BadRequestException("El vehiculo no existe"));
        Vehiculo vehiculo = vehiculoMapper.toEntity(vehiculoDTO);
        vehiculo.setCreatedDate(existente.getCreatedDate());
        vehiculo.setLastModifiedDate(Instant.now());

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
                existente.setLastModifiedDate(Instant.now());
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
        Inventario inventario = inventarioRepository.findByVehiculoId(vehiculoId).orElseThrow(() -> new BadRequestException("Inventario no encontrado"));
        if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO || CondicionVehiculo.VENDIDO.equals(inventario.getVehiculo().getCondicion())) {
            throw new BadRequestException("No se puede reservar un vehiculo ya vendido");
        }
        if (inventario.getEstadoInventario() == EstadoInventario.RESERVADO) {
            throw new BadRequestException("El vehiculo ya tiene una reserva activa. Liberala antes de reasignarla");
        }
        if (inventario.getEstadoInventario() != EstadoInventario.DISPONIBLE || !Boolean.TRUE.equals(inventario.getDisponible())) {
            throw new BadRequestException("El vehiculo no esta disponible para reservar");
        }

        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new BadRequestException("El cliente no existe"));
        inventario.setEstadoInventario(EstadoInventario.RESERVADO);
        inventario.setDisponible(false);
        inventario.setClienteReserva(cliente);
        inventario.setFechaReserva(Instant.now());
        inventario.setFechaVencimientoReserva(Instant.now().plus(3, ChronoUnit.DAYS));
        inventarioRepository.save(inventario);

        syncVehiculo(inventario);
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

        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setDisponible(true);
        inventario.setClienteReserva(null);
        inventario.setFechaReserva(null);
        inventario.setFechaVencimientoReserva(null);
        inventarioRepository.save(inventario);

        syncVehiculo(inventario);
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

        switch (inventario.getEstadoInventario()) {
            case DISPONIBLE -> vehiculo.setCondicion(CondicionVehiculo.EN_VENTA);
            case RESERVADO -> vehiculo.setCondicion(CondicionVehiculo.RESERVADO);
            case VENDIDO -> vehiculo.setCondicion(CondicionVehiculo.VENDIDO);
        }

        vehiculoRepository.save(vehiculo);
    }

    private void validarVehiculo(VehiculoDTO vehiculoDTO, Long idActual) {
        if (vehiculoDTO == null) {
            throw new BadRequestException("El vehiculo es obligatorio");
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
        return patente.trim().toUpperCase(Locale.ROOT);
    }
}
