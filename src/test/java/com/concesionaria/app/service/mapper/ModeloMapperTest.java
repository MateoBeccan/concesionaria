package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.ModeloAsserts.*;
import static com.concesionaria.app.domain.ModeloTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModeloMapperTest {

    private ModeloMapper modeloMapper;

    @BeforeEach
    void setUp() {
        modeloMapper = new ModeloMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getModeloSample1();
        var actual = modeloMapper.toEntity(modeloMapper.toDto(expected));
        assertModeloAllPropertiesEquals(expected, actual);
    }
}
