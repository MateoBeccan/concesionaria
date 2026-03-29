package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.VehiculoAsserts.*;
import static com.concesionaria.app.domain.VehiculoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VehiculoMapperTest {

    private VehiculoMapper vehiculoMapper;

    @BeforeEach
    void setUp() {
        vehiculoMapper = new VehiculoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getVehiculoSample1();
        var actual = vehiculoMapper.toEntity(vehiculoMapper.toDto(expected));
        assertVehiculoAllPropertiesEquals(expected, actual);
    }
}
