package com.concesionaria.app.domain;

import static com.concesionaria.app.domain.MarcaTestSamples.*;
import static com.concesionaria.app.domain.ModeloTestSamples.*;
import static com.concesionaria.app.domain.VersionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ModeloTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Modelo.class);
        Modelo modelo1 = getModeloSample1();
        Modelo modelo2 = new Modelo();
        assertThat(modelo1).isNotEqualTo(modelo2);

        modelo2.setId(modelo1.getId());
        assertThat(modelo1).isEqualTo(modelo2);

        modelo2 = getModeloSample2();
        assertThat(modelo1).isNotEqualTo(modelo2);
    }

    @Test
    void marcaTest() {
        Modelo modelo = getModeloRandomSampleGenerator();
        Marca marcaBack = getMarcaRandomSampleGenerator();

        modelo.setMarca(marcaBack);
        assertThat(modelo.getMarca()).isEqualTo(marcaBack);

        modelo.marca(null);
        assertThat(modelo.getMarca()).isNull();
    }

    @Test
    void versionesTest() {
        Modelo modelo = getModeloRandomSampleGenerator();
        Version versionBack = getVersionRandomSampleGenerator();

        modelo.addVersiones(versionBack);
        assertThat(modelo.getVersioneses()).containsOnly(versionBack);
        assertThat(versionBack.getModeloses()).containsOnly(modelo);

        modelo.removeVersiones(versionBack);
        assertThat(modelo.getVersioneses()).doesNotContain(versionBack);
        assertThat(versionBack.getModeloses()).doesNotContain(modelo);

        modelo.versioneses(new HashSet<>(Set.of(versionBack)));
        assertThat(modelo.getVersioneses()).containsOnly(versionBack);
        assertThat(versionBack.getModeloses()).containsOnly(modelo);

        modelo.setVersioneses(new HashSet<>());
        assertThat(modelo.getVersioneses()).doesNotContain(versionBack);
        assertThat(versionBack.getModeloses()).doesNotContain(modelo);
    }
}
