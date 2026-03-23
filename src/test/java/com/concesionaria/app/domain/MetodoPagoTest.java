package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.MetodoPagoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MetodoPagoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MetodoPago.class);
        MetodoPago metodoPago1 = getMetodoPagoSample1();
        MetodoPago metodoPago2 = new MetodoPago();
        assertThat(metodoPago1).isNotEqualTo(metodoPago2);

        metodoPago2.setId(metodoPago1.getId());
        assertThat(metodoPago1).isEqualTo(metodoPago2);

        metodoPago2 = getMetodoPagoSample2();
        assertThat(metodoPago1).isNotEqualTo(metodoPago2);
    }
}
