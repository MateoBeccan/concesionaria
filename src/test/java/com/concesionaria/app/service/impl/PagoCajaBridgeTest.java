package com.concesionaria.app.service.impl;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.service.MovimientoCajaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagoCajaBridgeTest {

    @Mock
    private MovimientoCajaService movimientoCajaService;

    @Test
    void registrarPagoMonetarioEsIngreso() {
        PagoTextNormalizer normalizer = new PagoTextNormalizer();
        PagoMetodoPolicy policy = new PagoMetodoPolicy(null, normalizer);
        PagoCajaBridge bridge = new PagoCajaBridge(movimientoCajaService, policy);
        Pago pago = pago("CONTADO", TipoMovimientoPago.PAGO_RECIBIDO);

        bridge.registrarPago(pago);

        verify(movimientoCajaService).registrarDesdePago(pago, eq(TipoMovimientoCaja.INGRESO), eq(EstadoPago.REGISTRADO), eq(true));
    }

    @Test
    void registrarAnulacionNoMonetariaEsInformativo() {
        PagoTextNormalizer normalizer = new PagoTextNormalizer();
        PagoMetodoPolicy policy = new PagoMetodoPolicy(null, normalizer);
        PagoCajaBridge bridge = new PagoCajaBridge(movimientoCajaService, policy);
        Pago pago = pago("PLAN_AHORRO", TipoMovimientoPago.ANULACION);

        bridge.registrarAnulacion(pago);

        verify(movimientoCajaService).registrarDesdePago(pago, eq(TipoMovimientoCaja.INFORMATIVO), eq(EstadoPago.ANULADO), eq(false));
    }

    private Pago pago(String metodo, TipoMovimientoPago tipo) {
        Pago pago = new Pago();
        pago.setTipoMovimiento(tipo);
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setCodigo(metodo);
        pago.setMetodoPago(metodoPago);
        return pago;
    }
}
