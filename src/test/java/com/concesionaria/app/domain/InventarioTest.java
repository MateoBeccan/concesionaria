package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.ClienteTestSamples.*;
import static com.concesionaria.app.domain.InventarioTestSamples.*;
import static com.concesionaria.app.domain.VehiculoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InventarioTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Inventario.class);
        Inventario inventario1 = getInventarioSample1();
        Inventario inventario2 = new Inventario();
        assertThat(inventario1).isNotEqualTo(inventario2);

        inventario2.setId(inventario1.getId());
        assertThat(inventario1).isEqualTo(inventario2);

        inventario2 = getInventarioSample2();
        assertThat(inventario1).isNotEqualTo(inventario2);
    }

    @Test
    void vehiculoTest() {
        Inventario inventario = getInventarioRandomSampleGenerator();
        Vehiculo vehiculoBack = getVehiculoRandomSampleGenerator();

        inventario.setVehiculo(vehiculoBack);
        assertThat(inventario.getVehiculo()).isEqualTo(vehiculoBack);

        inventario.vehiculo(null);
        assertThat(inventario.getVehiculo()).isNull();
    }

    @Test
    void clienteReservaTest() {
        Inventario inventario = getInventarioRandomSampleGenerator();
        Cliente clienteBack = getClienteRandomSampleGenerator();

        inventario.setClienteReserva(clienteBack);
        assertThat(inventario.getClienteReserva()).isEqualTo(clienteBack);

        inventario.clienteReserva(null);
        assertThat(inventario.getClienteReserva()).isNull();
    }
}
