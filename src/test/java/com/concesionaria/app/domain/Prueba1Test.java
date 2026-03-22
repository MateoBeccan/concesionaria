package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.Prueba1TestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class Prueba1Test {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Prueba1.class);
        Prueba1 prueba11 = getPrueba1Sample1();
        Prueba1 prueba12 = new Prueba1();
        assertThat(prueba11).isNotEqualTo(prueba12);

        prueba12.setId(prueba11.getId());
        assertThat(prueba11).isEqualTo(prueba12);

        prueba12 = getPrueba1Sample2();
        assertThat(prueba11).isNotEqualTo(prueba12);
    }

    @Test
    void hashCodeVerifier() {
        Prueba1 prueba1 = new Prueba1();
        assertThat(prueba1.hashCode()).isZero();

        Prueba1 prueba11 = getPrueba1Sample1();
        prueba1.setId(prueba11.getId());
        assertThat(prueba1).hasSameHashCodeAs(prueba11);
    }
}
