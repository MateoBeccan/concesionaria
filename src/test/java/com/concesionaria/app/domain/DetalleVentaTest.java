package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.DetalleVentaTestSamples.*;
import static com.concesionaria.app.domain.VehiculoTestSamples.*;
import static com.concesionaria.app.domain.VentaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DetalleVentaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DetalleVenta.class);
        DetalleVenta detalleVenta1 = getDetalleVentaSample1();
        DetalleVenta detalleVenta2 = new DetalleVenta();
        assertThat(detalleVenta1).isNotEqualTo(detalleVenta2);

        detalleVenta2.setId(detalleVenta1.getId());
        assertThat(detalleVenta1).isEqualTo(detalleVenta2);

        detalleVenta2 = getDetalleVentaSample2();
        assertThat(detalleVenta1).isNotEqualTo(detalleVenta2);
    }

    @Test
    void ventaTest() {
        DetalleVenta detalleVenta = getDetalleVentaRandomSampleGenerator();
        Venta ventaBack = getVentaRandomSampleGenerator();

        detalleVenta.setVenta(ventaBack);
        assertThat(detalleVenta.getVenta()).isEqualTo(ventaBack);

        detalleVenta.venta(null);
        assertThat(detalleVenta.getVenta()).isNull();
    }

    @Test
    void vehiculoTest() {
        DetalleVenta detalleVenta = getDetalleVentaRandomSampleGenerator();
        Vehiculo vehiculoBack = getVehiculoRandomSampleGenerator();

        detalleVenta.setVehiculo(vehiculoBack);
        assertThat(detalleVenta.getVehiculo()).isEqualTo(vehiculoBack);

        detalleVenta.vehiculo(null);
        assertThat(detalleVenta.getVehiculo()).isNull();
    }
}
