package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.ClienteTestSamples.*;
import static com.concesionaria.app.domain.CondicionIvaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClienteTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Cliente.class);
        Cliente cliente1 = getClienteSample1();
        Cliente cliente2 = new Cliente();
        assertThat(cliente1).isNotEqualTo(cliente2);

        cliente2.setId(cliente1.getId());
        assertThat(cliente1).isEqualTo(cliente2);

        cliente2 = getClienteSample2();
        assertThat(cliente1).isNotEqualTo(cliente2);
    }

    @Test
    void condicionIvaTest() {
        Cliente cliente = getClienteRandomSampleGenerator();
        CondicionIva condicionIvaBack = getCondicionIvaRandomSampleGenerator();

        cliente.setCondicionIva(condicionIvaBack);
        assertThat(cliente.getCondicionIva()).isEqualTo(condicionIvaBack);

        cliente.condicionIva(null);
        assertThat(cliente.getCondicionIva()).isNull();
    }
}
