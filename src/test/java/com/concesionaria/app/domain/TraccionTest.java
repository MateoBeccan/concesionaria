package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.TraccionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TraccionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Traccion.class);
        Traccion traccion1 = getTraccionSample1();
        Traccion traccion2 = new Traccion();
        assertThat(traccion1).isNotEqualTo(traccion2);

        traccion2.setId(traccion1.getId());
        assertThat(traccion1).isEqualTo(traccion2);

        traccion2 = getTraccionSample2();
        assertThat(traccion1).isNotEqualTo(traccion2);
    }
}
