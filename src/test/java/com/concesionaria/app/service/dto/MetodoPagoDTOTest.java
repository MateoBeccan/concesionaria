package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MetodoPagoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MetodoPagoDTO.class);
        MetodoPagoDTO metodoPagoDTO1 = new MetodoPagoDTO();
        metodoPagoDTO1.setId(1L);
        MetodoPagoDTO metodoPagoDTO2 = new MetodoPagoDTO();
        assertThat(metodoPagoDTO1).isNotEqualTo(metodoPagoDTO2);
        metodoPagoDTO2.setId(metodoPagoDTO1.getId());
        assertThat(metodoPagoDTO1).isEqualTo(metodoPagoDTO2);
        metodoPagoDTO2.setId(2L);
        assertThat(metodoPagoDTO1).isNotEqualTo(metodoPagoDTO2);
        metodoPagoDTO1.setId(null);
        assertThat(metodoPagoDTO1).isNotEqualTo(metodoPagoDTO2);
    }
}
