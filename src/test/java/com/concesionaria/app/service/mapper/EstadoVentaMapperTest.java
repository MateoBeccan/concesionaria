package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.EstadoVentaAsserts.*;
import static com.concesionaria.app.domain.EstadoVentaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EstadoVentaMapperTest {

    private EstadoVentaMapper estadoVentaMapper;

    @BeforeEach
    void setUp() {
        estadoVentaMapper = new EstadoVentaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEstadoVentaSample1();
        var actual = estadoVentaMapper.toEntity(estadoVentaMapper.toDto(expected));
        assertEstadoVentaAllPropertiesEquals(expected, actual);
    }
}
