package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.PagoAsserts.*;
import static com.concesionaria.app.domain.PagoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PagoMapperTest {

    private PagoMapper pagoMapper;

    @BeforeEach
    void setUp() {
        pagoMapper = new PagoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPagoSample1();
        var actual = pagoMapper.toEntity(pagoMapper.toDto(expected));
        assertPagoAllPropertiesEquals(expected, actual);
    }
}
