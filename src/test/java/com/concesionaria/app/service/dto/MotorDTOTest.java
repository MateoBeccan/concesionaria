package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MotorDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MotorDTO.class);
        MotorDTO motorDTO1 = new MotorDTO();
        motorDTO1.setId(1L);
        MotorDTO motorDTO2 = new MotorDTO();
        assertThat(motorDTO1).isNotEqualTo(motorDTO2);
        motorDTO2.setId(motorDTO1.getId());
        assertThat(motorDTO1).isEqualTo(motorDTO2);
        motorDTO2.setId(2L);
        assertThat(motorDTO1).isNotEqualTo(motorDTO2);
        motorDTO1.setId(null);
        assertThat(motorDTO1).isNotEqualTo(motorDTO2);
    }
}
