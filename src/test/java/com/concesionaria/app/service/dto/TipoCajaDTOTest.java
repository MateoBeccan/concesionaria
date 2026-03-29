package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TipoCajaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TipoCajaDTO.class);
        TipoCajaDTO tipoCajaDTO1 = new TipoCajaDTO();
        tipoCajaDTO1.setId(1L);
        TipoCajaDTO tipoCajaDTO2 = new TipoCajaDTO();
        assertThat(tipoCajaDTO1).isNotEqualTo(tipoCajaDTO2);
        tipoCajaDTO2.setId(tipoCajaDTO1.getId());
        assertThat(tipoCajaDTO1).isEqualTo(tipoCajaDTO2);
        tipoCajaDTO2.setId(2L);
        assertThat(tipoCajaDTO1).isNotEqualTo(tipoCajaDTO2);
        tipoCajaDTO1.setId(null);
        assertThat(tipoCajaDTO1).isNotEqualTo(tipoCajaDTO2);
    }
}
