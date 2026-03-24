package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.CombustibleTestSamples.*;
import static com.concesionaria.app.domain.MotorTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
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
    void combustibleTest() {
        Motor motor = getMotorRandomSampleGenerator();
        Combustible combustibleBack = getCombustibleRandomSampleGenerator();

        motor.setCombustible(combustibleBack);
        assertThat(motor.getCombustible()).isEqualTo(combustibleBack);

        motor.combustible(null);
        assertThat(motor.getCombustible()).isNull();
    }
}
