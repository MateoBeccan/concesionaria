package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.ModeloTestSamples.*;
import static com.concesionaria.app.domain.VersionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VersionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Version.class);
        Version version1 = getVersionSample1();
        Version version2 = new Version();
        assertThat(version1).isNotEqualTo(version2);

        version2.setId(version1.getId());
        assertThat(version1).isEqualTo(version2);

        version2 = getVersionSample2();
        assertThat(version1).isNotEqualTo(version2);
    }

    @Test
    void modeloTest() {
        Version version = getVersionRandomSampleGenerator();
        Modelo modeloBack = getModeloRandomSampleGenerator();

        version.setModelo(modeloBack);
        assertThat(version.getModelo()).isEqualTo(modeloBack);

        version.modelo(null);
        assertThat(version.getModelo()).isNull();
    }
}
