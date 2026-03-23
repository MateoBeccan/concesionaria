package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CotizacionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CotizacionDTO.class);
        CotizacionDTO cotizacionDTO1 = new CotizacionDTO();
        cotizacionDTO1.setId(1L);
        CotizacionDTO cotizacionDTO2 = new CotizacionDTO();
        assertThat(cotizacionDTO1).isNotEqualTo(cotizacionDTO2);
        cotizacionDTO2.setId(cotizacionDTO1.getId());
        assertThat(cotizacionDTO1).isEqualTo(cotizacionDTO2);
        cotizacionDTO2.setId(2L);
        assertThat(cotizacionDTO1).isNotEqualTo(cotizacionDTO2);
        cotizacionDTO1.setId(null);
        assertThat(cotizacionDTO1).isNotEqualTo(cotizacionDTO2);
    }
}
