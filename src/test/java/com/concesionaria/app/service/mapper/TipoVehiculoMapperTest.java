package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.TipoVehiculoAsserts.*;
import static com.concesionaria.app.domain.TipoVehiculoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TipoVehiculoMapperTest {

    private TipoVehiculoMapper tipoVehiculoMapper;

    @BeforeEach
    void setUp() {
        tipoVehiculoMapper = new TipoVehiculoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTipoVehiculoSample1();
        var actual = tipoVehiculoMapper.toEntity(tipoVehiculoMapper.toDto(expected));
        assertTipoVehiculoAllPropertiesEquals(expected, actual);
    }
}
