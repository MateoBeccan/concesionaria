package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.CarroceriaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CarroceriaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Carroceria.class);
        Carroceria carroceria1 = getCarroceriaSample1();
        Carroceria carroceria2 = new Carroceria();
        assertThat(carroceria1).isNotEqualTo(carroceria2);

        carroceria2.setId(carroceria1.getId());
        assertThat(carroceria1).isEqualTo(carroceria2);

        carroceria2 = getCarroceriaSample2();
        assertThat(carroceria1).isNotEqualTo(carroceria2);
    }
}
