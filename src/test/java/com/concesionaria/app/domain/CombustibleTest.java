package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.CombustibleTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CombustibleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Combustible.class);
        Combustible combustible1 = getCombustibleSample1();
        Combustible combustible2 = new Combustible();
        assertThat(combustible1).isNotEqualTo(combustible2);

        combustible2.setId(combustible1.getId());
        assertThat(combustible1).isEqualTo(combustible2);

        combustible2 = getCombustibleSample2();
        assertThat(combustible1).isNotEqualTo(combustible2);
    }
}
