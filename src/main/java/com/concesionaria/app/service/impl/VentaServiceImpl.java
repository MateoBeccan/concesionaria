package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.*;
import com.concesionaria.app.domain.enumeration.CondicionVehiculo;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.*;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.VentaMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final VentaMapper ventaMapper;
    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;
    private final ClienteRepository clienteRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final CotizacionRepository cotizacionRepository;

    public VentaServiceImpl(
        VentaRepository ventaRepository,
        VentaMapper ventaMapper,
        VehiculoRepository vehiculoRepository,
        InventarioRepository inventarioRepository,
        ClienteRepository clienteRepository,
        DetalleVentaRepository detalleVentaRepository,
        CotizacionRepository cotizacionRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.ventaMapper = ventaMapper;
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.clienteRepository = clienteRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.cotizacionRepository = cotizacionRepository;
    }

    // ================= CRUD =================

    @Override
    public VentaDTO save(VentaDTO dto) {
        validarVentaDto(dto);
        return ventaMapper.toDto(ventaRepository.save(ventaMapper.toEntity(dto)));
    }

    @Override
    public VentaDTO update(VentaDTO dto) {
        validarVentaDto(dto);
        return ventaMapper.toDto(ventaRepository.save(ventaMapper.toEntity(dto)));
    }

    @Override
    public Optional<VentaDTO> partialUpdate(VentaDTO dto) {
        return ventaRepository.findById(dto.getId())
            .map(existing -> {
                ventaMapper.partialUpdate(existing, dto);
                validarVentaDto(ventaMapper.toDto(existing));
                return existing;
            })
            .map(ventaRepository::save)
            .map(ventaMapper::toDto);
    }

    private void validarVentaDto(VentaDTO dto) {
        if (dto.getCliente() == null || dto.getCliente().getId() == null) {
            throw new BadRequestException("El cliente es obligatorio");
        }

        if (dto.getFecha() == null) {
            throw new BadRequestException("La fecha de la venta es obligatoria");
        }

        if (dto.getEstado() == null) {
            throw new BadRequestException("El estado de la venta es obligatorio");
        }

        if (dto.getImporteNeto() == null || dto.getImpuesto() == null || dto.getTotal() == null) {
            throw new BadRequestException("Los importes de la venta son obligatorios");
        }

        if (dto.getImporteNeto().compareTo(BigDecimal.ZERO) <= 0 || dto.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La venta debe tener un total mayor a 0");
        }

        BigDecimal totalCalculado = dto.getImporteNeto().add(dto.getImpuesto()).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal totalActual = dto.getTotal().setScale(2, java.math.RoundingMode.HALF_UP);

        if (totalCalculado.compareTo(totalActual) != 0) {
            throw new BadRequestException("El total debe coincidir con importe neto + impuesto");
        }
    }

    @Override
    public Page<VentaDTO> findAll(Pageable pageable) {
        return ventaRepository.findAll(pageable).map(ventaMapper::toDto);
    }

    @Override
    public Page<VentaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return ventaRepository.findAllWithEagerRelationships(pageable)
            .map(ventaMapper::toDto);
    }

    @Override
    public Optional<VentaDTO> findOne(Long id) {
        return ventaRepository.findById(id).map(ventaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        ventaRepository.deleteById(id);
    }

    // ================= NEGOCIO =================

    @Override
    public VentaDTO crearVenta(Long vehiculoId, Long clienteId) {

        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new RuntimeException("Cliente no existe"));

        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
            .orElseThrow(() -> new RuntimeException("Vehiculo no existe"));

        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoId)
            .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        //  VALIDACIONES
        if (inv.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new RuntimeException("Vehiculo ya vendido");
        }

        if (inv.getEstadoInventario() == EstadoInventario.DISPONIBLE && !Boolean.TRUE.equals(inv.getDisponible())) {
            throw new RuntimeException("El inventario del vehiculo esta inconsistente y no figura disponible para vender");
        }

        if (inv.getEstadoInventario() == EstadoInventario.RESERVADO &&
            (inv.getClienteReserva() == null || !inv.getClienteReserva().getId().equals(clienteId))) {
            throw new RuntimeException("Reservado por otro cliente");
        }

        if (
            inv.getEstadoInventario() == EstadoInventario.RESERVADO &&
            (Boolean.TRUE.equals(inv.getDisponible()) || inv.getFechaReserva() == null || inv.getFechaVencimientoReserva() == null)
        ) {
            throw new RuntimeException("La reserva actual del vehiculo esta incompleta y debe corregirse antes de vender");
        }

        if (vehiculo.getCondicion() == CondicionVehiculo.VENDIDO) {
            throw new RuntimeException("Vehiculo ya vendido");
        }

        //  CÁLCULOS
        BigDecimal precio = vehiculo.getPrecio();
        BigDecimal impuesto = precio.multiply(new BigDecimal("0.21"));
        BigDecimal total = precio.add(impuesto);

        //  CREAR VENTA
        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setFecha(Instant.now());
        venta.setCreatedDate(Instant.now());
        venta.setLastModifiedDate(Instant.now());

        venta.setImporteNeto(precio);
        venta.setImpuesto(impuesto);
        venta.setTotal(total);

        venta.setTotalPagado(BigDecimal.ZERO);
        venta.setSaldo(total);
        venta.setEstado(EstadoVenta.PENDIENTE);

        //  COTIZACIÓN
        Cotizacion cotizacion = cotizacionRepository
            .findTopByOrderByFechaDesc()
            .orElseThrow(() -> new RuntimeException("No hay cotización"));

        venta.setCotizacion(cotizacion.getValorVenta());


        Venta saved = ventaRepository.save(venta);

        // DETALLE
        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(saved);
        detalle.setVehiculo(vehiculo);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(precio);
        detalle.setSubtotal(precio);

        detalleVentaRepository.save(detalle);

        // INVENTARIO → RESERVADO (flujo con pago)
        inv.setEstadoInventario(EstadoInventario.RESERVADO);
        inv.setDisponible(false);
        inv.setClienteReserva(cliente);
        if (inv.getFechaReserva() == null) {
            inv.setFechaReserva(Instant.now());
        }
        if (inv.getFechaVencimientoReserva() == null || !inv.getFechaVencimientoReserva().isAfter(inv.getFechaReserva())) {
            inv.setFechaVencimientoReserva(inv.getFechaReserva().plus(3, ChronoUnit.DAYS));
        }
        inventarioRepository.save(inv);

        //  VEHICULO → RESERVADO
        vehiculo.setCondicion(CondicionVehiculo.RESERVADO);
        vehiculoRepository.save(vehiculo);

        return ventaMapper.toDto(saved);
    }

    // CONFIRMAR VENTA (cuando se paga)
    @Override
    public void confirmarVenta(Long ventaId) {

        Venta venta = ventaRepository.findById(ventaId)
            .orElseThrow(() -> new RuntimeException("Venta no existe"));

        if (venta.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("La venta no está totalmente paga");
        }

        DetalleVenta detalle = detalleVentaRepository
            .findFirstByVentaId(ventaId)
            .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));

        Long vehiculoId = detalle.getVehiculo().getId();

        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoId)
            .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        //  INVENTARIO → VENDIDO
        inv.setEstadoInventario(EstadoInventario.VENDIDO);
        inv.setDisponible(false);
        inv.setClienteReserva(null);
        inv.setFechaReserva(null);
        inv.setFechaVencimientoReserva(null);
        inventarioRepository.save(inv);

        // VEHICULO → VENDIDO
        Vehiculo vehiculo = inv.getVehiculo();
        vehiculo.setCondicion(CondicionVehiculo.VENDIDO);
        vehiculoRepository.save(vehiculo);

        //  VENTA → FINALIZADA
        venta.setEstado(EstadoVenta.FINALIZADA);
        venta.setLastModifiedDate(Instant.now());

        ventaRepository.save(venta);
    }
}
