package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.ComprobanteTestSamples.*;
import static com.concesionaria.app.domain.MonedaTestSamples.*;
import static com.concesionaria.app.domain.TipoComprobanteTestSamples.*;
import static com.concesionaria.app.domain.VentaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ComprobanteTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Comprobante.class);
        Comprobante comprobante1 = getComprobanteSample1();
        Comprobante comprobante2 = new Comprobante();
        assertThat(comprobante1).isNotEqualTo(comprobante2);

        comprobante2.setId(comprobante1.getId());
        assertThat(comprobante1).isEqualTo(comprobante2);

        comprobante2 = getComprobanteSample2();
        assertThat(comprobante1).isNotEqualTo(comprobante2);
    }

    @Test
    void ventaTest() {
        Comprobante comprobante = getComprobanteRandomSampleGenerator();
        Venta ventaBack = getVentaRandomSampleGenerator();

        comprobante.setVenta(ventaBack);
        assertThat(comprobante.getVenta()).isEqualTo(ventaBack);

        comprobante.venta(null);
        assertThat(comprobante.getVenta()).isNull();
    }

    @Test
    void tipoComprobanteTest() {
        Comprobante comprobante = getComprobanteRandomSampleGenerator();
        TipoComprobante tipoComprobanteBack = getTipoComprobanteRandomSampleGenerator();

        comprobante.setTipoComprobante(tipoComprobanteBack);
        assertThat(comprobante.getTipoComprobante()).isEqualTo(tipoComprobanteBack);

        comprobante.tipoComprobante(null);
        assertThat(comprobante.getTipoComprobante()).isNull();
    }

    @Test
    void monedaTest() {
        Comprobante comprobante = getComprobanteRandomSampleGenerator();
        Moneda monedaBack = getMonedaRandomSampleGenerator();

        comprobante.setMoneda(monedaBack);
        assertThat(comprobante.getMoneda()).isEqualTo(monedaBack);

        comprobante.moneda(null);
        assertThat(comprobante.getMoneda()).isNull();
    }
}
