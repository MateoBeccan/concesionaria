package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.ModeloTestSamples.*;
import static com.concesionaria.app.domain.MotorTestSamples.*;
import static com.concesionaria.app.domain.VersionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
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
    void motoresTest() {
        Version version = getVersionRandomSampleGenerator();
        Motor motorBack = getMotorRandomSampleGenerator();

        version.addMotores(motorBack);
        assertThat(version.getMotoreses()).containsOnly(motorBack);

        version.removeMotores(motorBack);
        assertThat(version.getMotoreses()).doesNotContain(motorBack);

        version.motoreses(new HashSet<>(Set.of(motorBack)));
        assertThat(version.getMotoreses()).containsOnly(motorBack);

        version.setMotoreses(new HashSet<>());
        assertThat(version.getMotoreses()).doesNotContain(motorBack);
    }

    @Test
    void modelosTest() {
        Version version = getVersionRandomSampleGenerator();
        Modelo modeloBack = getModeloRandomSampleGenerator();

        version.addModelos(modeloBack);
        assertThat(version.getModeloses()).containsOnly(modeloBack);
        assertThat(modeloBack.getVersioneses()).containsOnly(version);

        version.removeModelos(modeloBack);
        assertThat(version.getModeloses()).doesNotContain(modeloBack);
        assertThat(modeloBack.getVersioneses()).doesNotContain(version);

        version.modeloses(new HashSet<>(Set.of(modeloBack)));
        assertThat(version.getModeloses()).containsOnly(modeloBack);
        assertThat(modeloBack.getVersioneses()).containsOnly(version);

        version.setModeloses(new HashSet<>());
        assertThat(version.getModeloses()).doesNotContain(modeloBack);
        assertThat(modeloBack.getVersioneses()).doesNotContain(version);
    }
}
