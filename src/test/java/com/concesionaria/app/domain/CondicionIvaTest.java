package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.CondicionIvaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CondicionIvaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CondicionIva.class);
        CondicionIva condicionIva1 = getCondicionIvaSample1();
        CondicionIva condicionIva2 = new CondicionIva();
        assertThat(condicionIva1).isNotEqualTo(condicionIva2);

        condicionIva2.setId(condicionIva1.getId());
        assertThat(condicionIva1).isEqualTo(condicionIva2);

        condicionIva2 = getCondicionIvaSample2();
        assertThat(condicionIva1).isNotEqualTo(condicionIva2);
    }
}
