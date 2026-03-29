package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.TipoVehiculoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TipoVehiculoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TipoVehiculo.class);
        TipoVehiculo tipoVehiculo1 = getTipoVehiculoSample1();
        TipoVehiculo tipoVehiculo2 = new TipoVehiculo();
        assertThat(tipoVehiculo1).isNotEqualTo(tipoVehiculo2);

        tipoVehiculo2.setId(tipoVehiculo1.getId());
        assertThat(tipoVehiculo1).isEqualTo(tipoVehiculo2);

        tipoVehiculo2 = getTipoVehiculoSample2();
        assertThat(tipoVehiculo1).isNotEqualTo(tipoVehiculo2);
    }
}
