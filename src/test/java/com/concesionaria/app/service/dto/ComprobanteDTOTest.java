package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ComprobanteDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ComprobanteDTO.class);
        ComprobanteDTO comprobanteDTO1 = new ComprobanteDTO();
        comprobanteDTO1.setId(1L);
        ComprobanteDTO comprobanteDTO2 = new ComprobanteDTO();
        assertThat(comprobanteDTO1).isNotEqualTo(comprobanteDTO2);
        comprobanteDTO2.setId(comprobanteDTO1.getId());
        assertThat(comprobanteDTO1).isEqualTo(comprobanteDTO2);
        comprobanteDTO2.setId(2L);
        assertThat(comprobanteDTO1).isNotEqualTo(comprobanteDTO2);
        comprobanteDTO1.setId(null);
        assertThat(comprobanteDTO1).isNotEqualTo(comprobanteDTO2);
    }
}
