package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.InventarioHistorial;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.InventarioHistorialRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.InventarioVentaSyncService;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InventarioVentaSyncServiceImpl implements InventarioVentaSyncService {

    private final VentaRepository ventaRepository;
    private final InventarioRepository inventarioRepository;
    private final ReservaRepository reservaRepository;
    private final InventarioHistorialRepository inventarioHistorialRepository;
    private final PagoRepository pagoRepository;

    @Value("${app.negocio.reserva.porcentaje-minimo:0.10}")
    private BigDecimal porcentajeMinimoReserva = new BigDecimal("0.10");

    public InventarioVentaSyncServiceImpl(
        VentaRepository ventaRepository,
        InventarioRepository inventarioRepository,
        ReservaRepository reservaRepository,
        InventarioHistorialRepository inventarioHistorialRepository,
        PagoRepository pagoRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.inventarioRepository = inventarioRepository;
        this.reservaRepository = reservaRepository;
        this.inventarioHistorialRepository = inventarioHistorialRepository;
        this.pagoRepository = pagoRepository;
    }

    @Override
    public void sincronizarInventarioConVenta(Long ventaId, String usuarioLogin) {
        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta no existe"));
        Set<Long> vehiculoIds = vehiculoIdsDesdeVenta(venta);
        if (vehiculoIds.isEmpty()) {
            return;
        }
        Instant ahora = Instant.now();

        for (Long vehiculoId : vehiculoIds) {
            Inventario inventario = inventarioRepository.findByVehiculoId(vehiculoId).orElseThrow(() -> new BadRequestException("Inventario no encontrado"));

            normalizarReservaVencida(inventario, usuarioLogin, ahora);
            EstadoInventario estadoAnterior = inventario.getEstadoInventario();

            switch (venta.getEstado()) {
                case PAGADA, FINALIZADA -> marcarInventarioVendido(inventario, usuarioLogin, ahora);
                case PENDIENTE, RESERVADA -> {
                    if (cumpleMinimoReserva(venta)) {
                        marcarInventarioReservado(inventario, venta.getCliente(), venta, usuarioLogin, ahora);
                    } else {
                        liberarInventario(inventario, usuarioLogin, ahora);
                    }
                }
                case CANCELADA -> liberarInventario(inventario, usuarioLogin, ahora);
            }

            EstadoVenta estadoEsperado = calcularEstadoSegunPagos(venta);
            if (venta.getEstado() != estadoEsperado) {
                venta.setEstado(estadoEsperado);
                venta.setLastModifiedDate(ahora);
                venta.setLastModifiedBy(usuarioLogin);
                ventaRepository.save(venta);
            }

            inventario.setLastModifiedDate(ahora);
            inventario.setLastModifiedBy(usuarioLogin);
            inventarioRepository.save(inventario);

            if (estadoAnterior != inventario.getEstadoInventario()) {
                registrarHistorial(
                    inventario,
                    estadoAnterior,
                    inventario.getEstadoInventario(),
                    accionPorEstadoVenta(venta.getEstado()),
                    "Sincronizacion automatica desde venta " + venta.getId(),
                    usuarioLogin,
                    ahora
                );
            }
        }
    }

    private Set<Long> vehiculoIdsDesdeVenta(Venta venta) {
        if (venta == null || venta.getVehiculo() == null || venta.getVehiculo().getId() == null) {
            return Set.of();
        }
        return Set.of(venta.getVehiculo().getId());
    }

    private void marcarInventarioReservado(Inventario inventario, Cliente cliente, Venta venta, String usuarioLogin, Instant ahora) {
        if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new BadRequestException("No se puede reservar un vehiculo vendido");
        }
        inventario.setEstadoInventario(EstadoInventario.RESERVADO);
        upsertReservaActiva(inventario, cliente, venta, usuarioLogin, ahora);
    }

    private void marcarInventarioVendido(Inventario inventario, String usuarioLogin, Instant ahora) {
        inventario.setEstadoInventario(EstadoInventario.VENDIDO);
        cerrarReservaActiva(inventario, EstadoReserva.CONVERTIDA, usuarioLogin, ahora, null);
    }

    private void liberarInventario(Inventario inventario, String usuarioLogin, Instant ahora) {
        if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
            return;
        }
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        cerrarReservaActiva(inventario, EstadoReserva.CANCELADA, usuarioLogin, ahora, null);
    }

    private void registrarHistorial(
        Inventario inventario,
        EstadoInventario estadoAnterior,
        EstadoInventario estadoNuevo,
        String accion,
        String detalle,
        String usuarioLogin,
        Instant ahora
    ) {
        InventarioHistorial historial = new InventarioHistorial();
        historial.setInventario(inventario);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setAccion(accion);
        historial.setDetalle(detalle);
        historial.setMotivo(detalle);
        reservaRepository
            .findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(inventario.getId(), EstadoReserva.ACTIVA)
            .map(Reserva::getCliente)
            .ifPresent(historial::setCliente);
        historial.setFecha(ahora);
        historial.setUsuario(usuarioLogin);
        inventarioHistorialRepository.save(historial);
    }

    private void normalizarReservaVencida(Inventario inventario, String usuarioLogin, Instant ahora) {
        if (inventario.getEstadoInventario() != EstadoInventario.RESERVADO) {
            return;
        }
        Optional<Reserva> reservaActivaOpt = reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(
            inventario.getId(),
            EstadoReserva.ACTIVA
        );
        if (reservaActivaOpt.isEmpty()) {
            return;
        }
        Reserva reservaActiva = reservaActivaOpt.get();
        Instant vencimiento = reservaActiva.getFechaVencimiento();
        if (vencimiento == null || vencimiento.isAfter(Instant.now())) {
            return;
        }
        if (inventario.getVehiculo() != null && inventario.getVehiculo().getId() != null) {
            boolean ventaActiva = ventaRepository.existsByVehiculoIdAndEstadoIn(
                inventario.getVehiculo().getId(),
                EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA, EstadoVenta.FINALIZADA)
            );
            if (ventaActiva) {
                return;
            }
        }

        EstadoInventario anterior = inventario.getEstadoInventario();
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setLastModifiedDate(ahora);
        inventario.setLastModifiedBy(usuarioLogin);
        inventarioRepository.save(inventario);
        reservaActiva.setEstado(EstadoReserva.VENCIDA);
        reservaActiva.setLastModifiedDate(ahora);
        reservaRepository.save(reservaActiva);
        registrarHistorial(inventario, anterior, EstadoInventario.DISPONIBLE, "RESERVA_EXPIRADA", "Reserva vencida automaticamente", usuarioLogin, ahora);
    }

    private void upsertReservaActiva(Inventario inventario, Cliente cliente, Venta venta, String usuarioLogin, Instant ahora) {
        if (inventario == null || inventario.getId() == null || cliente == null) {
            return;
        }
        Reserva reserva = reservaRepository
            .findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(inventario.getId(), EstadoReserva.ACTIVA)
            .orElseGet(Reserva::new);
        reserva.setInventario(inventario);
        reserva.setCliente(cliente);
        if (venta != null) {
            reserva.setMoneda(venta.getMoneda());
            if (venta.getId() != null) {
                reserva.setMontoSenia(totalPagadoRegistrado(venta.getId(), venta));
            } else if (venta.getTotalPagado() != null) {
                reserva.setMontoSenia(venta.getTotalPagado().setScale(2, RoundingMode.HALF_UP));
            }
        }
        if (reserva.getFechaReserva() == null) {
            reserva.setFechaReserva(ahora);
        }
        if (reserva.getFechaVencimiento() == null || !reserva.getFechaVencimiento().isAfter(reserva.getFechaReserva())) {
            reserva.setFechaVencimiento(plusOneMonth(reserva.getFechaReserva()));
        }
        reserva.setEstado(EstadoReserva.ACTIVA);
        reserva.setUsuarioCreacion(usuarioLogin);
        if (reserva.getCreatedDate() == null) {
            reserva.setCreatedDate(ahora);
        }
        reserva.setLastModifiedDate(ahora);
        reservaRepository.save(reserva);
    }

    private void cerrarReservaActiva(Inventario inventario, EstadoReserva estado, String usuarioLogin, Instant ahora, Venta venta) {
        if (inventario == null || inventario.getId() == null) {
            return;
        }
        reservaRepository
            .findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(inventario.getId(), EstadoReserva.ACTIVA)
            .ifPresent(reserva -> {
                reserva.setEstado(estado);
                if (venta != null && venta.getMoneda() != null) {
                    reserva.setMoneda(venta.getMoneda());
                }
                reserva.setLastModifiedDate(ahora);
                reservaRepository.save(reserva);
            });
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
        return venta.getImporteNeto() == null ? BigDecimal.ZERO : venta.getImporteNeto();
    }

    private BigDecimal calcularMontoMinimoReserva(BigDecimal base) {
        BigDecimal importeBase = base == null ? BigDecimal.ZERO : base;
        return importeBase.multiply(porcentajeMinimoReserva).setScale(2, RoundingMode.HALF_UP);
    }

    private String accionPorEstadoVenta(EstadoVenta estadoVenta) {
        return switch (estadoVenta) {
            case PENDIENTE, RESERVADA -> "VENTA_RESERVADA";
            case PAGADA, FINALIZADA -> "VENTA_CONFIRMADA";
            case CANCELADA -> "VENTA_CANCELADA";
        };
    }

    private Instant plusOneMonth(Instant base) {
        return base.atZone(ZoneOffset.UTC).plusMonths(1).toInstant();
    }
}

