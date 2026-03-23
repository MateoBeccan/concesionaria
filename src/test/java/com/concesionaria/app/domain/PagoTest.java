package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.MetodoPagoTestSamples.*;
import static com.concesionaria.app.domain.MonedaTestSamples.*;
import static com.concesionaria.app.domain.PagoTestSamples.*;
import static com.concesionaria.app.domain.VentaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PagoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Pago.class);
        Pago pago1 = getPagoSample1();
        Pago pago2 = new Pago();
        assertThat(pago1).isNotEqualTo(pago2);

        pago2.setId(pago1.getId());
        assertThat(pago1).isEqualTo(pago2);

        pago2 = getPagoSample2();
        assertThat(pago1).isNotEqualTo(pago2);
    }

    @Test
    void ventaTest() {
        Pago pago = getPagoRandomSampleGenerator();
        Venta ventaBack = getVentaRandomSampleGenerator();

        pago.setVenta(ventaBack);
        assertThat(pago.getVenta()).isEqualTo(ventaBack);

        pago.venta(null);
        assertThat(pago.getVenta()).isNull();
    }

    @Test
    void metodoPagoTest() {
        Pago pago = getPagoRandomSampleGenerator();
        MetodoPago metodoPagoBack = getMetodoPagoRandomSampleGenerator();

        pago.setMetodoPago(metodoPagoBack);
        assertThat(pago.getMetodoPago()).isEqualTo(metodoPagoBack);

        pago.metodoPago(null);
        assertThat(pago.getMetodoPago()).isNull();
    }

    @Test
    void monedaTest() {
        Pago pago = getPagoRandomSampleGenerator();
        Moneda monedaBack = getMonedaRandomSampleGenerator();

        pago.setMoneda(monedaBack);
        assertThat(pago.getMoneda()).isEqualTo(monedaBack);

        pago.moneda(null);
        assertThat(pago.getMoneda()).isNull();
    }
}
