package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.*;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.*;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.VentaMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VentaServiceImpl implements VentaService {

    private static final Logger LOG = LoggerFactory.getLogger(VentaServiceImpl.class);

    private final VentaRepository ventaRepository;
    private final VentaMapper ventaMapper;

    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;
    private final ClienteRepository clienteRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public VentaServiceImpl(
        VentaRepository ventaRepository,
        VentaMapper ventaMapper,
        VehiculoRepository vehiculoRepository,
        InventarioRepository inventarioRepository,
        ClienteRepository clienteRepository,
        DetalleVentaRepository detalleVentaRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.ventaMapper = ventaMapper;
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.clienteRepository = clienteRepository;
        this.detalleVentaRepository = detalleVentaRepository;
    }

    // =========================
    // CRUD BASE
    // =========================

    @Override
    public VentaDTO save(VentaDTO ventaDTO) {
        Venta venta = ventaMapper.toEntity(ventaDTO);
        return ventaMapper.toDto(ventaRepository.save(venta));
    }

    @Override
    public VentaDTO update(VentaDTO ventaDTO) {
        Venta venta = ventaMapper.toEntity(ventaDTO);
        return ventaMapper.toDto(ventaRepository.save(venta));
    }

    @Override
    public Optional<VentaDTO> partialUpdate(VentaDTO ventaDTO) {
        return ventaRepository
            .findById(ventaDTO.getId())
            .map(existing -> {
                ventaMapper.partialUpdate(existing, ventaDTO);
                return existing;
            })
            .map(ventaRepository::save)
            .map(ventaMapper::toDto);
    }

    @Override
    public Page<VentaDTO> findAll(Pageable pageable) {
        return ventaRepository.findAll(pageable).map(ventaMapper::toDto);
    }

    @Override
    public Page<VentaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return ventaRepository.findAllWithEagerRelationships(pageable).map(ventaMapper::toDto);
    }

    @Override
    public Optional<VentaDTO> findOne(Long id) {
        return ventaRepository.findOneWithEagerRelationships(id).map(ventaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        ventaRepository.deleteById(id);
    }

    // =========================
    // LÓGICA DE NEGOCIO
    // =========================

    @Override
    public VentaDTO crearVenta(Long vehiculoId, Long clienteId) {

        LOG.info("Creando venta vehiculo {} cliente {}", vehiculoId, clienteId);

        // =========================
        // VALIDAR CLIENTE
        // =========================
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new BadRequestException("El cliente no existe"));

        // =========================
        // VALIDAR VEHICULO
        // =========================
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
            .orElseThrow(() -> new BadRequestException("El vehículo no existe"));

        if (vehiculo.getPrecio() == null || vehiculo.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El vehículo no tiene un precio válido");
        }

        // =========================
        // VALIDAR INVENTARIO
        // =========================
        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoId)
            .orElseThrow(() -> new BadRequestException("No existe inventario para el vehículo"));

        if (inv.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new BadRequestException("El vehículo ya fue vendido");
        }

        if (inv.getEstadoInventario() == EstadoInventario.RESERVADO) {
            if (inv.getClienteReserva() == null ||
                !inv.getClienteReserva().getId().equals(clienteId)) {

                throw new BadRequestException("El vehículo está reservado por otro cliente");
            }
        }

        // =========================
        // CREAR VENTA
        // =========================
        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setFecha(Instant.now());
        venta.setCreatedDate(Instant.now());
        venta.setEstado(EstadoVenta.PENDIENTE);

        BigDecimal precio = vehiculo.getPrecio();
        BigDecimal impuesto = BigDecimal.ZERO;
        BigDecimal total = precio.add(impuesto);

        venta.setImporteNeto(precio);
        venta.setImpuesto(impuesto);
        venta.setTotal(total);
        venta.setTotalPagado(BigDecimal.ZERO);
        venta.setSaldo(total);

        Venta savedVenta = ventaRepository.save(venta);

        // =========================
        // DETALLE VENTA
        // =========================
        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(savedVenta);
        detalle.setVehiculo(vehiculo);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(precio);
        detalle.setSubtotal(precio);

        detalleVentaRepository.save(detalle);

        // =========================
        // ACTUALIZAR INVENTARIO
        // =========================
        inv.setEstadoInventario(EstadoInventario.VENDIDO);
        inv.setDisponible(false);
        inv.setClienteReserva(null);
        inv.setFechaReserva(null);
        inv.setFechaVencimientoReserva(null);

        inventarioRepository.save(inv);

        LOG.info("Venta creada con ID {} para vehiculo {}", savedVenta.getId(), vehiculoId);

        return ventaMapper.toDto(savedVenta);
    }
}
