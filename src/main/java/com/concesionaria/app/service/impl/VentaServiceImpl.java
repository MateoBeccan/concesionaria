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
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.VentaMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CotizacionRepository cotizacionRepository;
    private final InventarioHistorialRepository inventarioHistorialRepository;
    private final PagoRepository pagoRepository;

    @Value("${app.negocio.reserva.porcentaje-minimo:0.10}")
    private BigDecimal porcentajeMinimoReserva = new BigDecimal("0.10");

    public VentaServiceImpl(
        VentaRepository ventaRepository,
        VentaMapper ventaMapper,
        VehiculoRepository vehiculoRepository,
        InventarioRepository inventarioRepository,
        ClienteRepository clienteRepository,
        DetalleVentaRepository detalleVentaRepository,
        CotizacionRepository cotizacionRepository,
        InventarioHistorialRepository inventarioHistorialRepository,
        PagoRepository pagoRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.ventaMapper = ventaMapper;
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.clienteRepository = clienteRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.cotizacionRepository = cotizacionRepository;
        this.inventarioHistorialRepository = inventarioHistorialRepository;
        this.pagoRepository = pagoRepository;
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
        Venta saved = ventaRepository.save(venta);
        sincronizarInventarioConVenta(saved.getId());
        return ventaMapper.toDto(saved);
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
        Venta saved = ventaRepository.save(venta);
        sincronizarInventarioConVenta(saved.getId());
        return ventaMapper.toDto(saved);
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
            .map(saved -> {
                sincronizarInventarioConVenta(saved.getId());
                return saved;
            })
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
        if (dto.getMoneda() == null || dto.getMoneda().getId() == null) {
            throw new BadRequestException("La moneda de la venta es obligatoria");
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

        BigDecimal totalPagado = dto.getTotalPagado() == null ? BigDecimal.ZERO : dto.getTotalPagado();
        BigDecimal saldo = dto.getSaldo() == null ? dto.getTotal().subtract(totalPagado) : dto.getSaldo();
        if (totalPagado.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El total pagado no puede ser negativo");
        }
        if (totalPagado.compareTo(BigDecimal.ZERO) == 0) {
            throw new BadRequestException(
                "La venta requiere un pago minimo del " +
                porcentajeMinimoReservaEscalaHumana().stripTrailingZeros().toPlainString() +
                "% del valor del vehiculo"
            );
        }
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El saldo no puede ser negativo");
        }
        if (totalPagado.compareTo(dto.getTotal()) > 0) {
            throw new BadRequestException("El total pagado no puede superar al total de la venta");
        }
        if (dto.getEstado() == EstadoVenta.PAGADA && saldo.compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("Una venta PAGADA debe tener saldo cero");
        }
        if (dto.getEstado() == EstadoVenta.CANCELADA && totalPagado.compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("No se puede marcar como CANCELADA una venta con pagos registrados");
        }
        BigDecimal montoMinimoReserva = calcularMontoMinimoReserva(dto.getImporteNeto());
        if (totalPagado.compareTo(BigDecimal.ZERO) > 0 && totalPagado.compareTo(montoMinimoReserva) < 0) {
            throw new BadRequestException(
                "La venta requiere una sena minima del " +
                porcentajeMinimoReservaEscalaHumana().stripTrailingZeros().toPlainString() +
                "% del valor del vehiculo"
            );
        }
        if (dto.getEstado() == EstadoVenta.RESERVADA) {
            if (totalPagado.compareTo(montoMinimoReserva) < 0) {
                throw new BadRequestException(
                    "La reserva requiere un pago minimo del " +
                    porcentajeMinimoReservaEscalaHumana().stripTrailingZeros().toPlainString() +
                    "% del valor del vehiculo"
                );
            }
        }

        dto.setTotalPagado(totalPagado);
        dto.setSaldo(saldo);
    }

    @Override
    public Page<VentaDTO> findAll(Pageable pageable) {
        Page<Venta> page = ventaRepository.findAll(pageable);
        reconciliarInventarioVentasActivas(page.getContent());
        return page.map(ventaMapper::toDto);
    }

    @Override
    public Page<VentaDTO> findAllWithEagerRelationships(Pageable pageable) {
        Page<Venta> page = ventaRepository.findAllWithEagerRelationships(pageable);
        reconciliarInventarioVentasActivas(page.getContent());
        return page.map(ventaMapper::toDto);
    }

    @Override
    public Optional<VentaDTO> findOne(Long id) {
        sincronizarInventarioConVenta(id);
        return ventaRepository.findOneWithEagerRelationships(id).map(ventaMapper::toDto);
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
        normalizarReservaVencida(inv);

        if (inv.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new BadRequestException("El vehiculo ya fue vendido");
        }
        if (inv.getEstadoInventario() == EstadoInventario.RESERVADO && (inv.getClienteReserva() == null || !inv.getClienteReserva().getId().equals(clienteId))) {
            throw new BadRequestException("El vehiculo esta reservado por otro cliente");
        }
        if (detalleVentaRepository.existsByVehiculoIdAndVentaEstadoIn(vehiculoId, EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA))) {
            throw new BadRequestException("El vehiculo ya tiene una venta activa asociada");
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
        venta.setMoneda(cotizacion.getMoneda());

        Venta saved = ventaRepository.save(venta);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(saved);
        detalle.setVehiculo(vehiculo);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(precio);
        detalle.setSubtotal(precio);
        detalleVentaRepository.save(detalle);

        sincronizarInventarioConVenta(saved.getId());

        return ventaMapper.toDto(saved);
    }

    @Override
    public void confirmarVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta no existe"));

        if (venta.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("La venta no esta totalmente paga");
        }
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new BadRequestException("No se puede confirmar una venta cancelada");
        }

        Set<Long> vehiculoIds = vehiculoIdsDesdeVenta(ventaId);
        for (Long vehiculoId : vehiculoIds) {
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
        }

        venta.setEstado(EstadoVenta.PAGADA);
        venta.setLastModifiedDate(Instant.now());
        venta.setLastModifiedBy(currentUserLogin());
        ventaRepository.save(venta);
    }

    @Override
    public void sincronizarInventarioConVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta no existe"));
        Set<Long> vehiculoIds = vehiculoIdsDesdeVenta(ventaId);
        if (vehiculoIds.isEmpty()) {
            return;
        }

        for (Long vehiculoId : vehiculoIds) {
            Inventario inventario = inventarioRepository.findByVehiculoId(vehiculoId).orElseThrow(() -> new BadRequestException("Inventario no encontrado"));

            normalizarReservaVencida(inventario);
            EstadoInventario estadoAnterior = inventario.getEstadoInventario();

            switch (venta.getEstado()) {
                case PAGADA, FINALIZADA -> marcarInventarioVendido(inventario);
                case PENDIENTE, RESERVADA -> {
                    if (cumpleMinimoReserva(venta)) {
                        marcarInventarioReservado(inventario, venta.getCliente());
                    } else {
                        liberarInventario(inventario);
                    }
                }
                case CANCELADA -> liberarInventario(inventario);
            }

            EstadoVenta estadoEsperado = calcularEstadoSegunPagos(venta);
            if (venta.getEstado() != estadoEsperado) {
                venta.setEstado(estadoEsperado);
                venta.setLastModifiedDate(Instant.now());
                venta.setLastModifiedBy(currentUserLogin());
                ventaRepository.save(venta);
            }

            inventario.setLastModifiedDate(Instant.now());
            inventario.setLastModifiedBy(currentUserLogin());
            inventarioRepository.save(inventario);
            syncCondicionCompat(inventario);

            if (estadoAnterior != inventario.getEstadoInventario()) {
                boolean reservaConSeniaConfirmada =
                    inventario.getEstadoInventario() == EstadoInventario.RESERVADO && (venta.getEstado() == EstadoVenta.PENDIENTE || venta.getEstado() == EstadoVenta.RESERVADA);
                registrarHistorial(
                    inventario,
                    estadoAnterior,
                    inventario.getEstadoInventario(),
                    reservaConSeniaConfirmada ? "RESERVA_CONFIRMADA" : accionPorEstadoVenta(venta.getEstado()),
                    reservaConSeniaConfirmada
                        ?
                        "Reserva generada con sena del " +
                        porcentajeMinimoReservaEscalaHumana().stripTrailingZeros().toPlainString() +
                        "%"
                        : "Sincronizacion automatica desde venta " + venta.getId()
                );
            }
        }
    }

    private Set<Long> vehiculoIdsDesdeVenta(Long ventaId) {
        List<DetalleVenta> detalles = detalleVentaRepository.findAllByVentaId(ventaId);
        if (detalles.isEmpty()) {
            return Set.of();
        }

        Set<Long> vehiculoIds = new LinkedHashSet<>();
        for (DetalleVenta detalle : detalles) {
            if (detalle.getVehiculo() != null && detalle.getVehiculo().getId() != null) {
                vehiculoIds.add(detalle.getVehiculo().getId());
            }
        }

        return vehiculoIds;
    }

    private void reconciliarInventarioVentasActivas(List<Venta> ventas) {
        for (Venta venta : ventas) {
            if (venta == null || venta.getId() == null || venta.getEstado() == null) {
                continue;
            }
            try {
                sincronizarInventarioConVenta(venta.getId());
            } catch (BadRequestException ex) {
                LOG.warn("No se pudo reconciliar inventario para venta {}: {}", venta.getId(), ex.getMessage());
            }
        }
    }

    private void marcarInventarioReservado(Inventario inventario, Cliente cliente) {
        if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new BadRequestException("No se puede reservar un vehiculo vendido");
        }
        inventario.setEstadoInventario(EstadoInventario.RESERVADO);
        inventario.setDisponible(false);
        inventario.setClienteReserva(cliente);
        if (inventario.getFechaReserva() == null) {
            inventario.setFechaReserva(Instant.now());
        }
        if (inventario.getFechaVencimientoReserva() == null || !inventario.getFechaVencimientoReserva().isAfter(inventario.getFechaReserva())) {
            inventario.setFechaVencimientoReserva(plusOneMonth(inventario.getFechaReserva()));
        }
    }

    private void marcarInventarioVendido(Inventario inventario) {
        inventario.setEstadoInventario(EstadoInventario.VENDIDO);
        inventario.setDisponible(false);
        inventario.setClienteReserva(null);
        inventario.setFechaReserva(null);
        inventario.setFechaVencimientoReserva(null);
    }

    private void liberarInventario(Inventario inventario) {
        if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
            return;
        }
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setDisponible(true);
        inventario.setClienteReserva(null);
        inventario.setFechaReserva(null);
        inventario.setFechaVencimientoReserva(null);
    }

    private String accionPorEstadoVenta(EstadoVenta estadoVenta) {
        return switch (estadoVenta) {
            case PENDIENTE, RESERVADA -> "VENTA_RESERVADA";
            case PAGADA, FINALIZADA -> "VENTA_CONFIRMADA";
            case CANCELADA -> "VENTA_CANCELADA";
        };
    }

    private void syncCondicionCompat(Inventario inventario) {
        Vehiculo vehiculo = inventario.getVehiculo();
        if (vehiculo == null) {
            return;
        }
        inventario.setDisponible(inventario.getEstadoInventario() == EstadoInventario.DISPONIBLE);
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

    private void normalizarReservaVencida(Inventario inventario) {
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
        syncCondicionCompat(inventario);
        registrarHistorial(inventario, anterior, EstadoInventario.DISPONIBLE, "RESERVA_EXPIRADA", "Reserva vencida automaticamente");
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }

    private Instant plusOneMonth(Instant base) {
        return base.atZone(ZoneOffset.UTC).plusMonths(1).toInstant();
    }

    private BigDecimal calcularMontoMinimoReserva(BigDecimal base) {
        BigDecimal importeBase = base == null ? BigDecimal.ZERO : base;
        return importeBase.multiply(porcentajeMinimoReserva).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal porcentajeMinimoReservaEscalaHumana() {
        return porcentajeMinimoReserva.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal totalPagadoRegistrado(Long ventaId, Venta venta) {
        BigDecimal totalPagado = pagoRepository.sumMontoByVentaId(ventaId);
        if (totalPagado == null) {
            totalPagado = BigDecimal.ZERO;
        }
        if (totalPagado.compareTo(BigDecimal.ZERO) == 0 && venta.getTotalPagado() != null) {
            totalPagado = venta.getTotalPagado();
        }
        return totalPagado.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean cumpleMinimoReserva(Venta venta) {
        if (venta == null || venta.getId() == null) {
            return false;
        }

        BigDecimal totalPagado = totalPagadoRegistrado(venta.getId(), venta);
        BigDecimal montoMinimo = calcularMontoMinimoReserva(calcularImporteBaseReserva(venta));
        return totalPagado.compareTo(montoMinimo) >= 0 && montoMinimo.compareTo(BigDecimal.ZERO) > 0;
    }

    private EstadoVenta calcularEstadoSegunPagos(Venta venta) {
        if (venta == null) {
            return EstadoVenta.PENDIENTE;
        }
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            return EstadoVenta.CANCELADA;
        }

        BigDecimal total = venta.getTotal() == null ? BigDecimal.ZERO : venta.getTotal();
        BigDecimal totalPagado = venta.getId() == null ? BigDecimal.ZERO : totalPagadoRegistrado(venta.getId(), venta);
        BigDecimal saldo = total.subtract(totalPagado).setScale(2, RoundingMode.HALF_UP);
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            saldo = BigDecimal.ZERO;
        }

        venta.setTotalPagado(totalPagado);
        venta.setSaldo(saldo);

        if (saldo.compareTo(BigDecimal.ZERO) == 0 && total.compareTo(BigDecimal.ZERO) > 0) {
            return EstadoVenta.PAGADA;
        }

        return cumpleMinimoReserva(venta) ? EstadoVenta.RESERVADA : EstadoVenta.PENDIENTE;
    }

    private BigDecimal calcularImporteBaseReserva(Venta venta) {
        if (venta == null || venta.getId() == null) {
            return BigDecimal.ZERO;
        }

        List<DetalleVenta> detalles = detalleVentaRepository.findAllByVentaId(venta.getId());
        BigDecimal totalDetalle = detalles
            .stream()
            .map(detalle -> {
                if (detalle.getSubtotal() != null) {
                    return detalle.getSubtotal();
                }
                if (detalle.getVehiculo() != null && detalle.getVehiculo().getPrecio() != null) {
                    return detalle.getVehiculo().getPrecio();
                }
                return BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDetalle.compareTo(BigDecimal.ZERO) > 0) {
            return totalDetalle;
        }

        return venta.getImporteNeto() == null ? BigDecimal.ZERO : venta.getImporteNeto();
    }
}
