package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.CombustibleTestSamples.*;
import static com.concesionaria.app.domain.MotorTestSamples.*;
import static com.concesionaria.app.domain.VersionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MotorTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Motor.class);
        Motor motor1 = getMotorSample1();
        Motor motor2 = new Motor();
        assertThat(motor1).isNotEqualTo(motor2);

        motor2.setId(motor1.getId());
        assertThat(motor1).isEqualTo(motor2);

        motor2 = getMotorSample2();
        assertThat(motor1).isNotEqualTo(motor2);
    }

    @Test
    void versionesTest() {
        Motor motor = getMotorRandomSampleGenerator();
        Version versionBack = getVersionRandomSampleGenerator();

        motor.addVersiones(versionBack);
        assertThat(motor.getVersioneses()).containsOnly(versionBack);
        assertThat(versionBack.getMotoreses()).containsOnly(motor);

        motor.removeVersiones(versionBack);
        assertThat(motor.getVersioneses()).doesNotContain(versionBack);
        assertThat(versionBack.getMotoreses()).doesNotContain(motor);

        motor.versioneses(new HashSet<>(Set.of(versionBack)));
        assertThat(motor.getVersioneses()).containsOnly(versionBack);
        assertThat(versionBack.getMotoreses()).containsOnly(motor);

        motor.setVersioneses(new HashSet<>());
        assertThat(motor.getVersioneses()).doesNotContain(versionBack);
        assertThat(versionBack.getMotoreses()).doesNotContain(motor);
    }

    @Test
    void combustiblesTest() {
        Motor motor = getMotorRandomSampleGenerator();
        Combustible combustibleBack = getCombustibleRandomSampleGenerator();

        motor.addCombustibles(combustibleBack);
        assertThat(motor.getCombustibleses()).containsOnly(combustibleBack);
        assertThat(combustibleBack.getMotor()).isEqualTo(motor);

        motor.removeCombustibles(combustibleBack);
        assertThat(motor.getCombustibleses()).doesNotContain(combustibleBack);
        assertThat(combustibleBack.getMotor()).isNull();

        motor.combustibleses(new HashSet<>(Set.of(combustibleBack)));
        assertThat(motor.getCombustibleses()).containsOnly(combustibleBack);
        assertThat(combustibleBack.getMotor()).isEqualTo(motor);

        motor.setCombustibleses(new HashSet<>());
        assertThat(motor.getCombustibleses()).doesNotContain(combustibleBack);
        assertThat(combustibleBack.getMotor()).isNull();
    }
}
