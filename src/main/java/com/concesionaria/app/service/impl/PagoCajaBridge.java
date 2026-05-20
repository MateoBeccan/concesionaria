package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.service.MovimientoCajaService;
import org.springframework.stereotype.Service;

@Service
public class PagoCajaBridge {

    private final MovimientoCajaService movimientoCajaService;
    private final PagoMetodoPolicy pagoMetodoPolicy;

    public PagoCajaBridge(MovimientoCajaService movimientoCajaService, PagoMetodoPolicy pagoMetodoPolicy) {
        this.movimientoCajaService = movimientoCajaService;
        this.pagoMetodoPolicy = pagoMetodoPolicy;
    }

    public void registrarPago(Pago pago) {
        boolean monetario = !pagoMetodoPolicy.esMetodoNoMonetarioInterno(pago.getMetodoPago());
        movimientoCajaService.registrarDesdePago(
            pago,
            monetario ? TipoMovimientoCaja.INGRESO : TipoMovimientoCaja.INFORMATIVO,
            EstadoPago.REGISTRADO,
            monetario
        );
    }

    public void registrarAnulacion(Pago pago) {
        boolean monetario = pago.getTipoMovimiento() != TipoMovimientoPago.ENTREGA_USADO &&
            !pagoMetodoPolicy.esMetodoNoMonetarioInterno(pago.getMetodoPago());
        movimientoCajaService.registrarDesdePago(
            pago,
            monetario ? TipoMovimientoCaja.REVERSO : TipoMovimientoCaja.INFORMATIVO,
            EstadoPago.ANULADO,
            monetario
        );
    }
}
