package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.TipoCajaAsserts.*;
import static com.concesionaria.app.domain.TipoCajaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TipoCajaMapperTest {

    private TipoCajaMapper tipoCajaMapper;

    @BeforeEach
    void setUp() {
        tipoCajaMapper = new TipoCajaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTipoCajaSample1();
        var actual = tipoCajaMapper.toEntity(tipoCajaMapper.toDto(expected));
        assertTipoCajaAllPropertiesEquals(expected, actual);
    }
}
