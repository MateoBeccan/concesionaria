package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.AutoTestSamples.*;
import static com.concesionaria.app.domain.ConfiguracionAutoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AutoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Auto.class);
        Auto auto1 = getAutoSample1();
        Auto auto2 = new Auto();
        assertThat(auto1).isNotEqualTo(auto2);

        auto2.setId(auto1.getId());
        assertThat(auto1).isEqualTo(auto2);

        auto2 = getAutoSample2();
        assertThat(auto1).isNotEqualTo(auto2);
    }

    @Test
    void configuracionTest() {
        Auto auto = getAutoRandomSampleGenerator();
        ConfiguracionAuto configuracionAutoBack = getConfiguracionAutoRandomSampleGenerator();

        auto.setConfiguracion(configuracionAutoBack);
        assertThat(auto.getConfiguracion()).isEqualTo(configuracionAutoBack);

        auto.configuracion(null);
        assertThat(auto.getConfiguracion()).isNull();
    }
}
