package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AutoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AutoDTO.class);
        AutoDTO autoDTO1 = new AutoDTO();
        autoDTO1.setId(1L);
        AutoDTO autoDTO2 = new AutoDTO();
        assertThat(autoDTO1).isNotEqualTo(autoDTO2);
        autoDTO2.setId(autoDTO1.getId());
        assertThat(autoDTO1).isEqualTo(autoDTO2);
        autoDTO2.setId(2L);
        assertThat(autoDTO1).isNotEqualTo(autoDTO2);
        autoDTO1.setId(null);
        assertThat(autoDTO1).isNotEqualTo(autoDTO2);
    }
}
