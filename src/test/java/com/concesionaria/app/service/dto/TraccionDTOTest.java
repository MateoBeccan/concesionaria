package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TraccionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TraccionDTO.class);
        TraccionDTO traccionDTO1 = new TraccionDTO();
        traccionDTO1.setId(1L);
        TraccionDTO traccionDTO2 = new TraccionDTO();
        assertThat(traccionDTO1).isNotEqualTo(traccionDTO2);
        traccionDTO2.setId(traccionDTO1.getId());
        assertThat(traccionDTO1).isEqualTo(traccionDTO2);
        traccionDTO2.setId(2L);
        assertThat(traccionDTO1).isNotEqualTo(traccionDTO2);
        traccionDTO1.setId(null);
        assertThat(traccionDTO1).isNotEqualTo(traccionDTO2);
    }
}
