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
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.VentaRepository;
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
public class VentaInventarioSyncService {
    private final VentaRepository ventaRepository;
    private final InventarioRepository inventarioRepository;
    private final ReservaRepository reservaRepository;
    private final InventarioHistorialRepository inventarioHistorialRepository;
    private final VentaCalculator ventaCalculator;
    private final VentaStateManager ventaStateManager;
    private final VentaHistorialService ventaHistorialService;

    @Value("${app.negocio.reserva.porcentaje-minimo:0.10}")
    private BigDecimal porcentajeMinimoReserva = new BigDecimal("0.10");

    public VentaInventarioSyncService(
        VentaRepository ventaRepository,
        InventarioRepository inventarioRepository,
        ReservaRepository reservaRepository,
        InventarioHistorialRepository inventarioHistorialRepository,
        VentaCalculator ventaCalculator,
        VentaStateManager ventaStateManager,
        VentaHistorialService ventaHistorialService
    ) {
        this.ventaRepository = ventaRepository;
        this.inventarioRepository = inventarioRepository;
        this.reservaRepository = reservaRepository;
        this.inventarioHistorialRepository = inventarioHistorialRepository;
        this.ventaCalculator = ventaCalculator;
        this.ventaStateManager = ventaStateManager;
        this.ventaHistorialService = ventaHistorialService;
    }

    public void sincronizarConVenta(Long ventaId) {
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
                case PAGADA, FINALIZADA -> marcarVendidoPorVenta(
                    venta,
                    "Reserva convertida en venta",
                    "Sincronizacion automatica desde venta " + venta.getId()
                );
                case PENDIENTE, RESERVADA -> {
                    if (cumpleMinimoReserva(venta)) {
                        reservarPorVenta(venta);
                    } else {
                        liberarPorCancelacion(venta);
                    }
                }
                case CANCELADA -> liberarPorCancelacion(venta);
            }

            EstadoVenta estadoEsperado = ventaStateManager.calcularEstadoSegunPagos(venta, porcentajeMinimoReserva);
            if (venta.getEstado() != estadoEsperado) {
                EstadoVenta estadoAnteriorVenta = venta.getEstado();
                venta.setEstado(estadoEsperado);
                venta.setLastModifiedDate(Instant.now());
                venta.setLastModifiedBy(currentUserLogin());
                ventaRepository.save(venta);
                ventaHistorialService.registrarCambioEstadoVentaSiCorresponde(
                    venta,
                    estadoAnteriorVenta,
                    "VENTA_ESTADO_RECALCULADO",
                    "Estado recalculado segun pagos e inventario",
                    this::currentUserLogin
                );
            }

            inventario = inventarioRepository.findByVehiculoId(vehiculoId).orElseThrow(() -> new BadRequestException("Inventario no encontrado"));
            inventario.setLastModifiedDate(Instant.now());
            inventario.setLastModifiedBy(currentUserLogin());
            inventarioRepository.save(inventario);

            boolean ventaCobrada = venta.getEstado() == EstadoVenta.PAGADA || venta.getEstado() == EstadoVenta.FINALIZADA;
            boolean historialVendidoYaRegistrado = ventaCobrada && inventario.getEstadoInventario() == EstadoInventario.VENDIDO;
            if (estadoAnterior != inventario.getEstadoInventario() && !historialVendidoYaRegistrado) {
                boolean reservaConSeniaConfirmada =
                    inventario.getEstadoInventario() == EstadoInventario.RESERVADO && (venta.getEstado() == EstadoVenta.PENDIENTE || venta.getEstado() == EstadoVenta.RESERVADA);
                registrarHistorialInventario(
                    inventario,
                    estadoAnterior,
                    inventario.getEstadoInventario(),
                    reservaConSeniaConfirmada ? "RESERVA_CONFIRMADA" : accionPorEstadoVenta(venta.getEstado()),
                    reservaConSeniaConfirmada
                        ? "Reserva generada con sena del " +
                        ventaCalculator.porcentajeMinimoReservaEscalaHumana(porcentajeMinimoReserva).stripTrailingZeros().toPlainString() +
                        "%"
                        : "Sincronizacion automatica desde venta " + venta.getId()
                );
            }
        }
    }

    public void reservarPorVenta(Venta venta) {
        Inventario inventario = inventarioVenta(venta);
        if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new BadRequestException("No se puede reservar un vehiculo vendido");
        }
        inventario.setEstadoInventario(EstadoInventario.RESERVADO);
        upsertReservaActiva(inventario, venta.getCliente(), venta);
        inventarioRepository.save(inventario);
    }

    public void marcarVendidoPorVenta(Venta venta) {
        marcarVendidoPorVenta(venta, "Reserva convertida en venta pagada", "Venta finalizada por pago completo");
    }

    private void marcarVendidoPorVenta(Venta venta, String detalleReserva, String detalleHistorial) {
        Inventario inventario = inventarioVenta(venta);
        EstadoInventario estadoAnterior = inventario.getEstadoInventario();
        inventario.setEstadoInventario(EstadoInventario.VENDIDO);
        cerrarReservaActiva(inventario, EstadoReserva.CONVERTIDA, detalleReserva, null);
        if (estadoAnterior != EstadoInventario.VENDIDO) {
            registrarHistorialInventario(inventario, estadoAnterior, EstadoInventario.VENDIDO, "VENTA_CONFIRMADA", detalleHistorial);
        }
        inventarioRepository.save(inventario);
    }

    public void liberarPorCancelacion(Venta venta) {
        Inventario inventario = inventarioVenta(venta);
        if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
            return;
        }
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        cerrarReservaActiva(inventario, EstadoReserva.CANCELADA, "Reserva liberada por cambio de estado de venta", null);
        inventarioRepository.save(inventario);
    }

    public void sincronizarReservaDesdeVenta(Venta venta) {
        if (venta == null || venta.getReserva() == null || venta.getReserva().getId() == null) {
            return;
        }
        Reserva reserva = reservaRepository.findById(venta.getReserva().getId()).orElse(null);
        if (reserva == null) {
            return;
        }
        if (venta.getMoneda() != null) {
            reserva.setMoneda(venta.getMoneda());
        }
        if (venta.getId() != null) {
            reserva.setMontoSenia(ventaCalculator.totalPagadoRegistrado(venta.getId(), venta));
        } else if (venta.getTotalPagado() != null) {
            reserva.setMontoSenia(venta.getTotalPagado().setScale(2, RoundingMode.HALF_UP));
        }
        reserva.setLastModifiedDate(Instant.now());
        reservaRepository.save(reserva);
    }

    public void normalizarReservaVencida(Inventario inventario) {
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
        inventario.setLastModifiedDate(Instant.now());
        inventario.setLastModifiedBy(currentUserLogin());
        inventarioRepository.save(inventario);
        reservaActiva.setEstado(EstadoReserva.VENCIDA);
        reservaActiva.setLastModifiedDate(Instant.now());
        reservaRepository.save(reservaActiva);
        registrarHistorialInventario(inventario, anterior, EstadoInventario.DISPONIBLE, "RESERVA_EXPIRADA", "Reserva vencida automaticamente");
    }

    public void registrarHistorialInventario(
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
        historial.setMotivo(detalle);
        reservaRepository
            .findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(inventario.getId(), EstadoReserva.ACTIVA)
            .map(Reserva::getCliente)
            .ifPresent(historial::setCliente);
        historial.setFecha(Instant.now());
        historial.setUsuario(currentUserLogin());
        inventarioHistorialRepository.save(historial);
    }

    private Inventario inventarioVenta(Venta venta) {
        if (venta == null || venta.getVehiculo() == null || venta.getVehiculo().getId() == null) {
            throw new BadRequestException("Inventario no encontrado");
        }
        return inventarioRepository.findByVehiculoId(venta.getVehiculo().getId()).orElseThrow(() -> new BadRequestException("Inventario no encontrado"));
    }

    private Set<Long> vehiculoIdsDesdeVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId).orElse(null);
        if (venta == null || venta.getVehiculo() == null || venta.getVehiculo().getId() == null) {
            return Set.of();
        }
        return Set.of(venta.getVehiculo().getId());
    }

    private boolean cumpleMinimoReserva(Venta venta) {
        if (venta == null || venta.getId() == null) {
            return false;
        }
        BigDecimal totalPagado = ventaCalculator.totalPagadoRegistrado(venta.getId(), venta);
        BigDecimal montoMinimo = ventaCalculator.calcularMontoMinimoReserva(ventaCalculator.calcularImporteBaseReserva(venta), porcentajeMinimoReserva);
        return totalPagado.compareTo(montoMinimo) >= 0 && montoMinimo.compareTo(BigDecimal.ZERO) > 0;
    }

    private void upsertReservaActiva(Inventario inventario, Cliente cliente, Venta venta) {
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
                reserva.setMontoSenia(ventaCalculator.totalPagadoRegistrado(venta.getId(), venta));
            } else if (venta.getTotalPagado() != null) {
                reserva.setMontoSenia(venta.getTotalPagado().setScale(2, RoundingMode.HALF_UP));
            }
        }
        if (reserva.getFechaReserva() == null) {
            reserva.setFechaReserva(Instant.now());
        }
        if (reserva.getFechaVencimiento() == null || !reserva.getFechaVencimiento().isAfter(reserva.getFechaReserva())) {
            reserva.setFechaVencimiento(plusOneMonth(reserva.getFechaReserva()));
        }
        reserva.setEstado(EstadoReserva.ACTIVA);
        reserva.setUsuarioCreacion(currentUserLogin());
        if (reserva.getCreatedDate() == null) {
            reserva.setCreatedDate(Instant.now());
        }
        reserva.setLastModifiedDate(Instant.now());
        reservaRepository.save(reserva);
    }

    private void cerrarReservaActiva(Inventario inventario, EstadoReserva estado, String detalle, Venta venta) {
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
                reserva.setLastModifiedDate(Instant.now());
                reservaRepository.save(reserva);
            });
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

    private String currentUserLogin() {
        return com.concesionaria.app.security.SecurityUtils.getCurrentUserLogin().orElse("system");
    }
}

