package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.TipoDocumentoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TipoDocumentoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TipoDocumento.class);
        TipoDocumento tipoDocumento1 = getTipoDocumentoSample1();
        TipoDocumento tipoDocumento2 = new TipoDocumento();
        assertThat(tipoDocumento1).isNotEqualTo(tipoDocumento2);

        tipoDocumento2.setId(tipoDocumento1.getId());
        assertThat(tipoDocumento1).isEqualTo(tipoDocumento2);

        tipoDocumento2 = getTipoDocumentoSample2();
        assertThat(tipoDocumento1).isNotEqualTo(tipoDocumento2);
    }
}
