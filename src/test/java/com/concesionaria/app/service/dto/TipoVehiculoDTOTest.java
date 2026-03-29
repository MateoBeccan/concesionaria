package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TipoVehiculoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TipoVehiculoDTO.class);
        TipoVehiculoDTO tipoVehiculoDTO1 = new TipoVehiculoDTO();
        tipoVehiculoDTO1.setId(1L);
        TipoVehiculoDTO tipoVehiculoDTO2 = new TipoVehiculoDTO();
        assertThat(tipoVehiculoDTO1).isNotEqualTo(tipoVehiculoDTO2);
        tipoVehiculoDTO2.setId(tipoVehiculoDTO1.getId());
        assertThat(tipoVehiculoDTO1).isEqualTo(tipoVehiculoDTO2);
        tipoVehiculoDTO2.setId(2L);
        assertThat(tipoVehiculoDTO1).isNotEqualTo(tipoVehiculoDTO2);
        tipoVehiculoDTO1.setId(null);
        assertThat(tipoVehiculoDTO1).isNotEqualTo(tipoVehiculoDTO2);
    }
}
