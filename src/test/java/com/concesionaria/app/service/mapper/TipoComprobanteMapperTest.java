package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.TipoComprobanteAsserts.*;
import static com.concesionaria.app.domain.TipoComprobanteTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TipoComprobanteMapperTest {

    private TipoComprobanteMapper tipoComprobanteMapper;

    @BeforeEach
    void setUp() {
        tipoComprobanteMapper = new TipoComprobanteMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTipoComprobanteSample1();
        var actual = tipoComprobanteMapper.toEntity(tipoComprobanteMapper.toDto(expected));
        assertTipoComprobanteAllPropertiesEquals(expected, actual);
    }
}
