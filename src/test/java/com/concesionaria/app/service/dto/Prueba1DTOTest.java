package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class Prueba1DTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(Prueba1DTO.class);
        Prueba1DTO prueba1DTO1 = new Prueba1DTO();
        prueba1DTO1.setId(1L);
        Prueba1DTO prueba1DTO2 = new Prueba1DTO();
        assertThat(prueba1DTO1).isNotEqualTo(prueba1DTO2);
        prueba1DTO2.setId(prueba1DTO1.getId());
        assertThat(prueba1DTO1).isEqualTo(prueba1DTO2);
        prueba1DTO2.setId(2L);
        assertThat(prueba1DTO1).isNotEqualTo(prueba1DTO2);
        prueba1DTO1.setId(null);
        assertThat(prueba1DTO1).isNotEqualTo(prueba1DTO2);
    }
}
