package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.AdjudicacionPlanAhorro;
import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.Cotizacion;
import com.concesionaria.app.domain.EntidadFinanciera;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import java.math.BigDecimal;
import java.time.Instant;

public record RegistrarPagoCommand(
    Instant fecha,
    BigDecimal monto,
    Moneda moneda,
    MetodoPago metodoPago,
    EntidadFinanciera entidadFinanciera,
    TipoMovimientoPago tipoMovimiento,
    EstadoPago estado,
    BigDecimal cotizacionUsada,
    BigDecimal montoAplicadoVenta,
    Instant fechaCotizacionUsada,
    Cotizacion cotizacionRef,
    Venta venta,
    Reserva reserva,
    TasacionUsado tasacionUsado,
    AdjudicacionPlanAhorro adjudicacionPlanAhorro,
    ContratoPlanAhorro contratoPlanAhorro,
    String usuarioRegistro,
    String referencia,
    String numeroOperacion,
    String comprobanteExterno,
    String bancoEntidad,
    String observaciones,
    String motivoAnulacion,
    String usuarioAnulacion,
    Instant fechaAnulacion,
    Instant createdDate,
    Instant lastModifiedDate
) {
    public static RegistrarPagoCommand from(Pago pago) {
        return new RegistrarPagoCommand(
            pago.getFecha(),
            pago.getMonto(),
            pago.getMoneda(),
            pago.getMetodoPago(),
            pago.getEntidadFinanciera(),
            pago.getTipoMovimiento(),
            pago.getEstado(),
            pago.getCotizacionUsada(),
            pago.getMontoAplicadoVenta(),
            pago.getFechaCotizacionUsada(),
            pago.getCotizacionRef(),
            pago.getVenta(),
            pago.getReserva(),
            pago.getTasacionUsado(),
            pago.getAdjudicacionPlanAhorro(),
            pago.getContratoPlanAhorro(),
            pago.getUsuarioRegistro(),
            pago.getReferencia(),
            pago.getNumeroOperacion(),
            pago.getComprobanteExterno(),
            pago.getBancoEntidad(),
            pago.getObservaciones(),
            pago.getMotivoAnulacion(),
            pago.getUsuarioAnulacion(),
            pago.getFechaAnulacion(),
            pago.getCreatedDate(),
            pago.getLastModifiedDate()
        );
    }
}
