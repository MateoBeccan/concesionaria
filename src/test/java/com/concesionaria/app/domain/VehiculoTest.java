package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.InventarioTestSamples.*;
import static com.concesionaria.app.domain.MotorTestSamples.*;
import static com.concesionaria.app.domain.TipoVehiculoTestSamples.*;
import static com.concesionaria.app.domain.VehiculoTestSamples.*;
import static com.concesionaria.app.domain.VersionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VehiculoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Vehiculo.class);
        Vehiculo vehiculo1 = getVehiculoSample1();
        Vehiculo vehiculo2 = new Vehiculo();
        assertThat(vehiculo1).isNotEqualTo(vehiculo2);

        vehiculo2.setId(vehiculo1.getId());
        assertThat(vehiculo1).isEqualTo(vehiculo2);

        vehiculo2 = getVehiculoSample2();
        assertThat(vehiculo1).isNotEqualTo(vehiculo2);
    }

    @Test
    void versionTest() {
        Vehiculo vehiculo = getVehiculoRandomSampleGenerator();
        Version versionBack = getVersionRandomSampleGenerator();

        vehiculo.setVersion(versionBack);
        assertThat(vehiculo.getVersion()).isEqualTo(versionBack);

        vehiculo.version(null);
        assertThat(vehiculo.getVersion()).isNull();
    }

    @Test
    void motorTest() {
        Vehiculo vehiculo = getVehiculoRandomSampleGenerator();
        Motor motorBack = getMotorRandomSampleGenerator();

        vehiculo.setMotor(motorBack);
        assertThat(vehiculo.getMotor()).isEqualTo(motorBack);

        vehiculo.motor(null);
        assertThat(vehiculo.getMotor()).isNull();
    }

    @Test
    void tipoVehiculoTest() {
        Vehiculo vehiculo = getVehiculoRandomSampleGenerator();
        TipoVehiculo tipoVehiculoBack = getTipoVehiculoRandomSampleGenerator();

        vehiculo.setTipoVehiculo(tipoVehiculoBack);
        assertThat(vehiculo.getTipoVehiculo()).isEqualTo(tipoVehiculoBack);

        vehiculo.tipoVehiculo(null);
        assertThat(vehiculo.getTipoVehiculo()).isNull();
    }

    @Test
    void inventarioTest() {
        Vehiculo vehiculo = getVehiculoRandomSampleGenerator();
        Inventario inventarioBack = getInventarioRandomSampleGenerator();

        vehiculo.setInventario(inventarioBack);
        assertThat(vehiculo.getInventario()).isEqualTo(inventarioBack);
        assertThat(inventarioBack.getVehiculo()).isEqualTo(vehiculo);

        vehiculo.inventario(null);
        assertThat(vehiculo.getInventario()).isNull();
        assertThat(inventarioBack.getVehiculo()).isNull();
    }
}
