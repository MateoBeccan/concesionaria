package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.CotizacionAsserts.*;
import static com.concesionaria.app.domain.CotizacionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CotizacionMapperTest {

    private CotizacionMapper cotizacionMapper;

    @BeforeEach
    void setUp() {
        cotizacionMapper = new CotizacionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCotizacionSample1();
        var actual = cotizacionMapper.toEntity(cotizacionMapper.toDto(expected));
        assertCotizacionAllPropertiesEquals(expected, actual);
    }
}
