package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.EstadoVentaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EstadoVentaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(EstadoVenta.class);
        EstadoVenta estadoVenta1 = getEstadoVentaSample1();
        EstadoVenta estadoVenta2 = new EstadoVenta();
        assertThat(estadoVenta1).isNotEqualTo(estadoVenta2);

        estadoVenta2.setId(estadoVenta1.getId());
        assertThat(estadoVenta1).isEqualTo(estadoVenta2);

        estadoVenta2 = getEstadoVentaSample2();
        assertThat(estadoVenta1).isNotEqualTo(estadoVenta2);
    }
}
