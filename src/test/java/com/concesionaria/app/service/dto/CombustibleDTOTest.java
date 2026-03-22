package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CombustibleDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CombustibleDTO.class);
        CombustibleDTO combustibleDTO1 = new CombustibleDTO();
        combustibleDTO1.setId(1L);
        CombustibleDTO combustibleDTO2 = new CombustibleDTO();
        assertThat(combustibleDTO1).isNotEqualTo(combustibleDTO2);
        combustibleDTO2.setId(combustibleDTO1.getId());
        assertThat(combustibleDTO1).isEqualTo(combustibleDTO2);
        combustibleDTO2.setId(2L);
        assertThat(combustibleDTO1).isNotEqualTo(combustibleDTO2);
        combustibleDTO1.setId(null);
        assertThat(combustibleDTO1).isNotEqualTo(combustibleDTO2);
    }
}
