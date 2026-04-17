package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Cotizacion;
import com.concesionaria.app.domain.DetalleVenta;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.InventarioHistorial;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.repository.CotizacionRepository;
import com.concesionaria.app.repository.DetalleVentaRepository;
import com.concesionaria.app.repository.InventarioHistorialRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.VentaMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final InventarioHistorialRepository inventarioHistorialRepository;

    public VentaServiceImpl(
        VentaRepository ventaRepository,
        VentaMapper ventaMapper,
        VehiculoRepository vehiculoRepository,
        InventarioRepository inventarioRepository,
        ClienteRepository clienteRepository,
        DetalleVentaRepository detalleVentaRepository,
        CotizacionRepository cotizacionRepository,
        InventarioHistorialRepository inventarioHistorialRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.ventaMapper = ventaMapper;
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.clienteRepository = clienteRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.cotizacionRepository = cotizacionRepository;
        this.inventarioHistorialRepository = inventarioHistorialRepository;
    }

    @Override
    public VentaDTO save(VentaDTO dto) {
        validarVentaDto(dto);
        Instant now = Instant.now();
        String currentUser = currentUserLogin();
        Venta venta = ventaMapper.toEntity(dto);
        venta.setCreatedDate(now);
        venta.setCreatedBy(currentUser);
        venta.setLastModifiedDate(now);
        venta.setLastModifiedBy(currentUser);
        return ventaMapper.toDto(ventaRepository.save(venta));
    }

    @Override
    public VentaDTO update(VentaDTO dto) {
        validarVentaDto(dto);
        Venta existing = ventaRepository.findById(dto.getId()).orElseThrow(() -> new BadRequestException("La venta no existe"));
        Venta venta = ventaMapper.toEntity(dto);
        venta.setCreatedDate(existing.getCreatedDate());
        venta.setCreatedBy(existing.getCreatedBy());
        venta.setLastModifiedDate(Instant.now());
        venta.setLastModifiedBy(currentUserLogin());
        return ventaMapper.toDto(ventaRepository.save(venta));
    }

    @Override
    public Optional<VentaDTO> partialUpdate(VentaDTO dto) {
        return ventaRepository
            .findById(dto.getId())
            .map(existing -> {
                ventaMapper.partialUpdate(existing, dto);
                validarVentaDto(ventaMapper.toDto(existing));
                if (existing.getCreatedBy() == null) {
                    existing.setCreatedBy(currentUserLogin());
                }
                existing.setLastModifiedDate(Instant.now());
                existing.setLastModifiedBy(currentUserLogin());
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
        return ventaRepository.findAllWithEagerRelationships(pageable).map(ventaMapper::toDto);
    }

    @Override
    public Optional<VentaDTO> findOne(Long id) {
        return ventaRepository.findById(id).map(ventaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        ventaRepository.deleteById(id);
    }

    @Override
    public VentaDTO crearVenta(Long vehiculoId, Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new BadRequestException("El cliente no existe"));
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId).orElseThrow(() -> new BadRequestException("El vehiculo no existe"));
        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoId).orElseThrow(() -> new BadRequestException("Inventario no encontrado"));

        if (inv.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new BadRequestException("El vehiculo ya fue vendido");
        }
        if (inv.getEstadoInventario() == EstadoInventario.RESERVADO && (inv.getClienteReserva() == null || !inv.getClienteReserva().getId().equals(clienteId))) {
            throw new BadRequestException("El vehiculo esta reservado por otro cliente");
        }

        BigDecimal precio = vehiculo.getPrecio();
        BigDecimal impuesto = precio.multiply(new BigDecimal("0.21"));
        BigDecimal total = precio.add(impuesto);

        Venta venta = new Venta();
        String currentUser = currentUserLogin();
        Instant now = Instant.now();
        venta.setCliente(cliente);
        venta.setFecha(now);
        venta.setCreatedDate(now);
        venta.setCreatedBy(currentUser);
        venta.setLastModifiedDate(now);
        venta.setLastModifiedBy(currentUser);
        venta.setImporteNeto(precio);
        venta.setImpuesto(impuesto);
        venta.setTotal(total);
        venta.setTotalPagado(BigDecimal.ZERO);
        venta.setSaldo(total);
        venta.setEstado(EstadoVenta.PENDIENTE);

        Cotizacion cotizacion = cotizacionRepository.findTopByOrderByFechaDesc().orElseThrow(() -> new BadRequestException("No hay cotizacion activa"));
        venta.setCotizacion(cotizacion.getValorVenta());

        Venta saved = ventaRepository.save(venta);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(saved);
        detalle.setVehiculo(vehiculo);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(precio);
        detalle.setSubtotal(precio);
        detalleVentaRepository.save(detalle);

        EstadoInventario estadoAnterior = inv.getEstadoInventario();
        inv.setEstadoInventario(EstadoInventario.RESERVADO);
        inv.setDisponible(false);
        inv.setClienteReserva(cliente);
        if (inv.getFechaReserva() == null) {
            inv.setFechaReserva(Instant.now());
        }
        if (inv.getFechaVencimientoReserva() == null || !inv.getFechaVencimientoReserva().isAfter(inv.getFechaReserva())) {
            inv.setFechaVencimientoReserva(inv.getFechaReserva().plus(3, ChronoUnit.DAYS));
        }
        inv.setLastModifiedDate(Instant.now());
        inv.setLastModifiedBy(currentUserLogin());
        inventarioRepository.save(inv);
        syncCondicionCompat(inv);
        registrarHistorial(inv, estadoAnterior, EstadoInventario.RESERVADO, "VENTA_CREADA", "Creacion de venta pendiente");

        return ventaMapper.toDto(saved);
    }

    @Override
    public void confirmarVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta no existe"));

        if (venta.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("La venta no esta totalmente paga");
        }

        DetalleVenta detalle = detalleVentaRepository.findFirstByVentaId(ventaId).orElseThrow(() -> new BadRequestException("Detalle de venta no encontrado"));
        Long vehiculoId = detalle.getVehiculo().getId();

        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoId).orElseThrow(() -> new BadRequestException("Inventario no encontrado"));
        EstadoInventario estadoAnterior = inv.getEstadoInventario();

        inv.setEstadoInventario(EstadoInventario.VENDIDO);
        inv.setDisponible(false);
        inv.setClienteReserva(null);
        inv.setFechaReserva(null);
        inv.setFechaVencimientoReserva(null);
        inv.setLastModifiedDate(Instant.now());
        inv.setLastModifiedBy(currentUserLogin());
        inventarioRepository.save(inv);
        syncCondicionCompat(inv);
        registrarHistorial(inv, estadoAnterior, EstadoInventario.VENDIDO, "VENTA_CONFIRMADA", "Venta finalizada por pago completo");

        venta.setEstado(EstadoVenta.PAGADA);
        venta.setLastModifiedDate(Instant.now());
        venta.setLastModifiedBy(currentUserLogin());
        ventaRepository.save(venta);
    }

    private void syncCondicionCompat(Inventario inventario) {
        Vehiculo vehiculo = inventario.getVehiculo();
        if (vehiculo == null) {
            return;
        }
        switch (inventario.getEstadoInventario()) {
            case DISPONIBLE -> vehiculo.setCondicion(com.concesionaria.app.domain.enumeration.CondicionVehiculo.EN_VENTA);
            case RESERVADO -> vehiculo.setCondicion(com.concesionaria.app.domain.enumeration.CondicionVehiculo.RESERVADO);
            case VENDIDO -> vehiculo.setCondicion(com.concesionaria.app.domain.enumeration.CondicionVehiculo.VENDIDO);
        }
        vehiculo.setLastModifiedDate(Instant.now());
        vehiculo.setLastModifiedBy(currentUserLogin());
        vehiculoRepository.save(vehiculo);
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

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }
}
