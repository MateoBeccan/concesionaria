package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.AutoTestSamples.*;
import static com.concesionaria.app.domain.MarcaTestSamples.*;
import static com.concesionaria.app.domain.ModeloTestSamples.*;
import static com.concesionaria.app.domain.MonedaTestSamples.*;
import static com.concesionaria.app.domain.MotorTestSamples.*;
import static com.concesionaria.app.domain.VersionTestSamples.*;
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
    void marcaTest() {
        Auto auto = getAutoRandomSampleGenerator();
        Marca marcaBack = getMarcaRandomSampleGenerator();

        auto.setMarca(marcaBack);
        assertThat(auto.getMarca()).isEqualTo(marcaBack);

        auto.marca(null);
        assertThat(auto.getMarca()).isNull();
    }

    @Test
    void modeloTest() {
        Auto auto = getAutoRandomSampleGenerator();
        Modelo modeloBack = getModeloRandomSampleGenerator();

        auto.setModelo(modeloBack);
        assertThat(auto.getModelo()).isEqualTo(modeloBack);

        auto.modelo(null);
        assertThat(auto.getModelo()).isNull();
    }

    @Test
    void versionTest() {
        Auto auto = getAutoRandomSampleGenerator();
        Version versionBack = getVersionRandomSampleGenerator();

        auto.setVersion(versionBack);
        assertThat(auto.getVersion()).isEqualTo(versionBack);

        auto.version(null);
        assertThat(auto.getVersion()).isNull();
    }

    @Test
    void motorTest() {
        Auto auto = getAutoRandomSampleGenerator();
        Motor motorBack = getMotorRandomSampleGenerator();

        auto.setMotor(motorBack);
        assertThat(auto.getMotor()).isEqualTo(motorBack);

        auto.motor(null);
        assertThat(auto.getMotor()).isNull();
    }

    @Test
    void monedaTest() {
        Auto auto = getAutoRandomSampleGenerator();
        Moneda monedaBack = getMonedaRandomSampleGenerator();

        auto.setMoneda(monedaBack);
        assertThat(auto.getMoneda()).isEqualTo(monedaBack);

        auto.moneda(null);
        assertThat(auto.getMoneda()).isNull();
    }
}
