package com.concesionaria.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InventarioDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(InventarioDTO.class);
        InventarioDTO inventarioDTO1 = new InventarioDTO();
        inventarioDTO1.setId(1L);
        InventarioDTO inventarioDTO2 = new InventarioDTO();
        assertThat(inventarioDTO1).isNotEqualTo(inventarioDTO2);
        inventarioDTO2.setId(inventarioDTO1.getId());
        assertThat(inventarioDTO1).isEqualTo(inventarioDTO2);
        inventarioDTO2.setId(2L);
        assertThat(inventarioDTO1).isNotEqualTo(inventarioDTO2);
        inventarioDTO1.setId(null);
        assertThat(inventarioDTO1).isNotEqualTo(inventarioDTO2);
    }
}
