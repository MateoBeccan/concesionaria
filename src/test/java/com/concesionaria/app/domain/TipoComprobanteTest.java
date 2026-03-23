package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.TipoComprobanteTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TipoComprobanteTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TipoComprobante.class);
        TipoComprobante tipoComprobante1 = getTipoComprobanteSample1();
        TipoComprobante tipoComprobante2 = new TipoComprobante();
        assertThat(tipoComprobante1).isNotEqualTo(tipoComprobante2);

        tipoComprobante2.setId(tipoComprobante1.getId());
        assertThat(tipoComprobante1).isEqualTo(tipoComprobante2);

        tipoComprobante2 = getTipoComprobanteSample2();
        assertThat(tipoComprobante1).isNotEqualTo(tipoComprobante2);
    }
}
