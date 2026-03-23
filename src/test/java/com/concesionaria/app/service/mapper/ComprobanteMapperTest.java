package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.ComprobanteAsserts.*;
import static com.concesionaria.app.domain.ComprobanteTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComprobanteMapperTest {

    private ComprobanteMapper comprobanteMapper;

    @BeforeEach
    void setUp() {
        comprobanteMapper = new ComprobanteMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getComprobanteSample1();
        var actual = comprobanteMapper.toEntity(comprobanteMapper.toDto(expected));
        assertComprobanteAllPropertiesEquals(expected, actual);
    }
}
