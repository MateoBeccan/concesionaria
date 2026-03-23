package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CondicionIvaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CondicionIvaDTO.class);
        CondicionIvaDTO condicionIvaDTO1 = new CondicionIvaDTO();
        condicionIvaDTO1.setId(1L);
        CondicionIvaDTO condicionIvaDTO2 = new CondicionIvaDTO();
        assertThat(condicionIvaDTO1).isNotEqualTo(condicionIvaDTO2);
        condicionIvaDTO2.setId(condicionIvaDTO1.getId());
        assertThat(condicionIvaDTO1).isEqualTo(condicionIvaDTO2);
        condicionIvaDTO2.setId(2L);
        assertThat(condicionIvaDTO1).isNotEqualTo(condicionIvaDTO2);
        condicionIvaDTO1.setId(null);
        assertThat(condicionIvaDTO1).isNotEqualTo(condicionIvaDTO2);
    }
}
