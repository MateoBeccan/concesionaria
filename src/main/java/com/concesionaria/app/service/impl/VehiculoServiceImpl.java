package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.*;
import com.concesionaria.app.domain.enumeration.CondicionVehiculo;
import com.concesionaria.app.domain.enumeration.EstadoInventario;

import com.concesionaria.app.repository.*;
import com.concesionaria.app.service.VehiculoService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.VehiculoDTO;

import com.concesionaria.app.service.mapper.VehiculoMapper;

import java.time.Instant;

import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.data.domain.*;
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

    @Override
    public VehiculoDTO update(VehiculoDTO vehiculoDTO) {
        return null;
    }

    @Override
    public Optional<VehiculoDTO> partialUpdate(VehiculoDTO vehiculoDTO) {
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculoDTO> findAll(Pageable pageable) {

        return vehiculoRepository
            .findAll(pageable)
            .map(vehiculoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehiculoDTO> findOne(Long id) {

        return vehiculoRepository
            .findById(id)
            .map(vehiculoMapper::toDto);
    }

    @Override
    public List<VehiculoDTO> findAllWhereInventarioIsNull() {
        return List.of();
    }



    @Override
    public void delete(Long id) {

    }



    @Override
    @Transactional(readOnly = true)
    public Optional<VehiculoDTO> findByPatente(String patente) {

        return vehiculoRepository
            .findByPatente(patente.toUpperCase().trim())
            .map(vehiculoMapper::toDto);
    }

    public VehiculoServiceImpl(
        VehiculoRepository vehiculoRepository,
        VehiculoMapper vehiculoMapper,
        InventarioRepository inventarioRepository,
        ClienteRepository clienteRepository,
        VentaService ventaService
    ) {
        this.vehiculoRepository = vehiculoRepository;
        this.vehiculoMapper = vehiculoMapper;
        this.inventarioRepository = inventarioRepository;
        this.clienteRepository = clienteRepository;
        this.ventaService = ventaService;
    }

    @Override
    public VehiculoDTO save(VehiculoDTO dto) {

        Vehiculo v = vehiculoMapper.toEntity(dto);
        v.setCreatedDate(Instant.now());

        Vehiculo saved = vehiculoRepository.save(v);

        Inventario inv = new Inventario();
        inv.setVehiculo(saved);
        inv.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inv.setDisponible(true);
        inv.setFechaIngreso(Instant.now());

        inventarioRepository.save(inv);

        return vehiculoMapper.toDto(saved);
    }

    @Override
    public void reservarVehiculo(Long vehiculoId, Long clienteId) {

        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoId)
            .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (inv.getEstadoInventario() != EstadoInventario.DISPONIBLE) {
            throw new RuntimeException("Vehiculo no disponible");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new RuntimeException("Cliente no existe"));

        inv.setEstadoInventario(EstadoInventario.RESERVADO);
        inv.setDisponible(false);
        inv.setClienteReserva(cliente);
        inv.setFechaReserva(Instant.now());
        inv.setFechaVencimientoReserva(Instant.now().plus(3, ChronoUnit.DAYS));

        inventarioRepository.save(inv);

        syncVehiculo(inv);
    }

    @Override
    public void venderVehiculo(Long vehiculoId, Long clienteId) {
        ventaService.crearVenta(vehiculoId, clienteId);
    }

    @Override
    public void cancelarReserva(Long vehiculoId) {

        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoId)
            .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (inv.getEstadoInventario() != EstadoInventario.RESERVADO) {
            throw new RuntimeException("No está reservado");
        }

        inv.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inv.setDisponible(true);
        inv.setClienteReserva(null);

        inventarioRepository.save(inv);

        syncVehiculo(inv);
    }

    private void syncVehiculo(Inventario inv) {

        Vehiculo v = inv.getVehiculo();

        switch (inv.getEstadoInventario()) {
            case DISPONIBLE -> v.setCondicion(CondicionVehiculo.EN_VENTA);
            case RESERVADO -> v.setCondicion(CondicionVehiculo.RESERVADO);
            case VENDIDO -> v.setCondicion(CondicionVehiculo.VENDIDO);
        }

        vehiculoRepository.save(v);
    }
}
