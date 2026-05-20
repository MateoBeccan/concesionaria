package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class VentaStateManager {

    private final VentaCalculator ventaCalculator;

    public VentaStateManager(VentaCalculator ventaCalculator) {
        this.ventaCalculator = ventaCalculator;
    }

    public EstadoVenta calcularEstadoSegunPagos(Venta venta, BigDecimal porcentajeMinimoReserva) {
        if (venta == null) {
            return EstadoVenta.PENDIENTE;
        }
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            return EstadoVenta.CANCELADA;
        }

        BigDecimal total = venta.getTotal() == null ? BigDecimal.ZERO : venta.getTotal();
        BigDecimal totalPagado = venta.getId() == null ? BigDecimal.ZERO : ventaCalculator.totalPagadoRegistrado(venta.getId(), venta);
        BigDecimal saldo = total.subtract(totalPagado).setScale(2, RoundingMode.HALF_UP);
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            saldo = BigDecimal.ZERO;
        }

        venta.setTotalPagado(totalPagado);
        venta.setSaldo(saldo);

        if (saldo.compareTo(BigDecimal.ZERO) == 0 && total.compareTo(BigDecimal.ZERO) > 0) {
            return EstadoVenta.PAGADA;
        }

        return cumpleMinimoReserva(venta, porcentajeMinimoReserva) ? EstadoVenta.RESERVADA : EstadoVenta.PENDIENTE;
    }

    private boolean cumpleMinimoReserva(Venta venta, BigDecimal porcentajeMinimoReserva) {
        if (venta == null || venta.getId() == null) {
            return false;
        }

        BigDecimal totalPagado = ventaCalculator.totalPagadoRegistrado(venta.getId(), venta);
        BigDecimal montoMinimo = ventaCalculator.calcularMontoMinimoReserva(ventaCalculator.calcularImporteBaseReserva(venta), porcentajeMinimoReserva);
        return totalPagado.compareTo(montoMinimo) >= 0 && montoMinimo.compareTo(BigDecimal.ZERO) > 0;
    }
}

