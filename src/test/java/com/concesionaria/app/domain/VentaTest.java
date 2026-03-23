package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.ClienteTestSamples.*;
import static com.concesionaria.app.domain.EstadoVentaTestSamples.*;
import static com.concesionaria.app.domain.MonedaTestSamples.*;
import static com.concesionaria.app.domain.VentaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VentaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Venta.class);
        Venta venta1 = getVentaSample1();
        Venta venta2 = new Venta();
        assertThat(venta1).isNotEqualTo(venta2);

        venta2.setId(venta1.getId());
        assertThat(venta1).isEqualTo(venta2);

        venta2 = getVentaSample2();
        assertThat(venta1).isNotEqualTo(venta2);
    }

    @Test
    void clienteTest() {
        Venta venta = getVentaRandomSampleGenerator();
        Cliente clienteBack = getClienteRandomSampleGenerator();

        venta.setCliente(clienteBack);
        assertThat(venta.getCliente()).isEqualTo(clienteBack);

        venta.cliente(null);
        assertThat(venta.getCliente()).isNull();
    }

    @Test
    void estadoVentaTest() {
        Venta venta = getVentaRandomSampleGenerator();
        EstadoVenta estadoVentaBack = getEstadoVentaRandomSampleGenerator();

        venta.setEstadoVenta(estadoVentaBack);
        assertThat(venta.getEstadoVenta()).isEqualTo(estadoVentaBack);

        venta.estadoVenta(null);
        assertThat(venta.getEstadoVenta()).isNull();
    }

    @Test
    void monedaTest() {
        Venta venta = getVentaRandomSampleGenerator();
        Moneda monedaBack = getMonedaRandomSampleGenerator();

        venta.setMoneda(monedaBack);
        assertThat(venta.getMoneda()).isEqualTo(monedaBack);

        venta.moneda(null);
        assertThat(venta.getMoneda()).isNull();
    }
}
