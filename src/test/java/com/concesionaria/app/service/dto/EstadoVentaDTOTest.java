package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EstadoVentaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(EstadoVentaDTO.class);
        EstadoVentaDTO estadoVentaDTO1 = new EstadoVentaDTO();
        estadoVentaDTO1.setId(1L);
        EstadoVentaDTO estadoVentaDTO2 = new EstadoVentaDTO();
        assertThat(estadoVentaDTO1).isNotEqualTo(estadoVentaDTO2);
        estadoVentaDTO2.setId(estadoVentaDTO1.getId());
        assertThat(estadoVentaDTO1).isEqualTo(estadoVentaDTO2);
        estadoVentaDTO2.setId(2L);
        assertThat(estadoVentaDTO1).isNotEqualTo(estadoVentaDTO2);
        estadoVentaDTO1.setId(null);
        assertThat(estadoVentaDTO1).isNotEqualTo(estadoVentaDTO2);
    }
}
