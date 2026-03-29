package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.TipoCajaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TipoCajaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TipoCaja.class);
        TipoCaja tipoCaja1 = getTipoCajaSample1();
        TipoCaja tipoCaja2 = new TipoCaja();
        assertThat(tipoCaja1).isNotEqualTo(tipoCaja2);

        tipoCaja2.setId(tipoCaja1.getId());
        assertThat(tipoCaja1).isEqualTo(tipoCaja2);

        tipoCaja2 = getTipoCajaSample2();
        assertThat(tipoCaja1).isNotEqualTo(tipoCaja2);
    }
}
