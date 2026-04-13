package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.enumeration.CondicionVehiculo;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.service.InventarioService;
import com.concesionaria.app.service.dto.InventarioDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.InventarioMapper;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Inventario}.
 */
@Service
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private static final Logger LOG = LoggerFactory.getLogger(InventarioServiceImpl.class);

    private final InventarioRepository inventarioRepository;
    private final InventarioMapper inventarioMapper;
    private final VehiculoRepository vehiculoRepository;
    private final ClienteRepository clienteRepository;

    public InventarioServiceImpl(
        InventarioRepository inventarioRepository,
        InventarioMapper inventarioMapper,
        VehiculoRepository vehiculoRepository,
        ClienteRepository clienteRepository
    ) {
        this.inventarioRepository = inventarioRepository;
        this.inventarioMapper = inventarioMapper;
        this.vehiculoRepository = vehiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public InventarioDTO save(InventarioDTO dto) {
        Inventario inventario = buildValidatedEntity(dto, null);
        inventario.setCreatedDate(Instant.now());
        inventario.setLastModifiedDate(Instant.now());
        Inventario saved = inventarioRepository.save(inventario);
        syncVehiculoCondicion(saved);
        return inventarioMapper.toDto(saved);
    }

    @Override
    public InventarioDTO update(InventarioDTO dto) {
        Inventario existente = inventarioRepository.findById(dto.getId()).orElseThrow(() -> new BadRequestException("El inventario no existe"));
        Inventario inventario = buildValidatedEntity(dto, existente);
        inventario.setCreatedDate(existente.getCreatedDate());
        inventario.setLastModifiedDate(Instant.now());
        Inventario saved = inventarioRepository.save(inventario);
        syncVehiculoCondicion(saved);
        return inventarioMapper.toDto(saved);
    }

    @Override
    public Optional<InventarioDTO> partialUpdate(InventarioDTO dto) {
        return inventarioRepository
            .findById(dto.getId())
            .map(existente -> {
                inventarioMapper.partialUpdate(existente, dto);
                validarYNormalizar(existente, dto.getId(), existente.getVehiculo(), existente.getClienteReserva());
                existente.setLastModifiedDate(Instant.now());
                Inventario saved = inventarioRepository.save(existente);
                syncVehiculoCondicion(saved);
                return inventarioMapper.toDto(saved);
            });
    }

    @Override
    public Page<InventarioDTO> findAll(Pageable pageable) {
        return inventarioRepository.findAll(pageable).map(inventarioMapper::toDto);
    }

    @Override
    public Optional<InventarioDTO> findOne(Long id) {
        return inventarioRepository.findById(id).map(inventarioMapper::toDto);
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
        inventario.setObservaciones(dto.getObservaciones());
        inventario.setDisponible(dto.getDisponible());
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

        if (CondicionVehiculo.VENDIDO.equals(vehiculo.getCondicion()) && inventario.getEstadoInventario() != EstadoInventario.VENDIDO) {
            throw new BadRequestException("No se puede marcar disponible o reservado un vehiculo ya vendido");
        }

        Inventario existingForVehicle = inventarioId != null ? inventarioRepository.findById(inventarioId).orElse(null) : null;
        if (
            existingForVehicle != null &&
            existingForVehicle.getEstadoInventario() == EstadoInventario.RESERVADO &&
            inventario.getEstadoInventario() == EstadoInventario.RESERVADO &&
            existingForVehicle.getClienteReserva() != null &&
            !Objects.equals(existingForVehicle.getClienteReserva().getId(), clienteReserva != null ? clienteReserva.getId() : null)
        ) {
            throw new BadRequestException("Liberá la reserva actual antes de asignarla a otro cliente");
        }

        switch (inventario.getEstadoInventario()) {
            case DISPONIBLE -> validarDisponible(inventario);
            case RESERVADO -> validarReservado(inventario, clienteReserva);
            case VENDIDO -> validarVendido(inventario);
            default -> throw new BadRequestException("El estado de inventario es obligatorio");
        }
    }

    private void validarDisponible(Inventario inventario) {
        if (!Boolean.TRUE.equals(inventario.getDisponible())) {
            throw new BadRequestException("Un vehiculo en venta debe figurar como disponible");
        }

        inventario.setClienteReserva(null);
        inventario.setFechaReserva(null);
        inventario.setFechaVencimientoReserva(null);
    }

    private void validarReservado(Inventario inventario, Cliente clienteReserva) {
        if (Boolean.TRUE.equals(inventario.getDisponible())) {
            throw new BadRequestException("Un vehiculo reservado no puede figurar como disponible");
        }

        if (clienteReserva == null) {
            throw new BadRequestException("Debés seleccionar un cliente para registrar la reserva");
        }

        if (inventario.getFechaReserva() == null) {
            throw new BadRequestException("La fecha de reserva es obligatoria cuando el inventario está reservado");
        }

        if (inventario.getFechaVencimientoReserva() == null) {
            throw new BadRequestException("La fecha de vencimiento de la reserva es obligatoria");
        }

        if (!inventario.getFechaVencimientoReserva().isAfter(inventario.getFechaReserva())) {
            throw new BadRequestException("El vencimiento de la reserva debe ser posterior a la fecha de reserva");
        }
    }

    private void validarVendido(Inventario inventario) {
        if (Boolean.TRUE.equals(inventario.getDisponible())) {
            throw new BadRequestException("Un vehiculo vendido no puede volver a figurar como disponible");
        }

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
            vehiculoRepository.save(vehiculo);
            LOG.debug("Vehiculo {} sincronizado a condicion {}", vehiculo.getId(), condicion);
        }
    }
}
