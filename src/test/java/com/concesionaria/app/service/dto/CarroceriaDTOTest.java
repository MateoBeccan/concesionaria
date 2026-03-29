package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CarroceriaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CarroceriaDTO.class);
        CarroceriaDTO carroceriaDTO1 = new CarroceriaDTO();
        carroceriaDTO1.setId(1L);
        CarroceriaDTO carroceriaDTO2 = new CarroceriaDTO();
        assertThat(carroceriaDTO1).isNotEqualTo(carroceriaDTO2);
        carroceriaDTO2.setId(carroceriaDTO1.getId());
        assertThat(carroceriaDTO1).isEqualTo(carroceriaDTO2);
        carroceriaDTO2.setId(2L);
        assertThat(carroceriaDTO1).isNotEqualTo(carroceriaDTO2);
        carroceriaDTO1.setId(null);
        assertThat(carroceriaDTO1).isNotEqualTo(carroceriaDTO2);
    }
}
