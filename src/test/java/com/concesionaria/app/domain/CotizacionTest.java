package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.CotizacionTestSamples.*;
import static com.concesionaria.app.domain.MonedaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CotizacionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Cotizacion.class);
        Cotizacion cotizacion1 = getCotizacionSample1();
        Cotizacion cotizacion2 = new Cotizacion();
        assertThat(cotizacion1).isNotEqualTo(cotizacion2);

        cotizacion2.setId(cotizacion1.getId());
        assertThat(cotizacion1).isEqualTo(cotizacion2);

        cotizacion2 = getCotizacionSample2();
        assertThat(cotizacion1).isNotEqualTo(cotizacion2);
    }

    @Test
    void monedaTest() {
        Cotizacion cotizacion = getCotizacionRandomSampleGenerator();
        Moneda monedaBack = getMonedaRandomSampleGenerator();

        cotizacion.setMoneda(monedaBack);
        assertThat(cotizacion.getMoneda()).isEqualTo(monedaBack);

        cotizacion.moneda(null);
        assertThat(cotizacion.getMoneda()).isNull();
    }
}
