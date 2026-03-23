package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.MetodoPagoAsserts.*;
import static com.concesionaria.app.domain.MetodoPagoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetodoPagoMapperTest {

    private MetodoPagoMapper metodoPagoMapper;

    @BeforeEach
    void setUp() {
        metodoPagoMapper = new MetodoPagoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMetodoPagoSample1();
        var actual = metodoPagoMapper.toEntity(metodoPagoMapper.toDto(expected));
        assertMetodoPagoAllPropertiesEquals(expected, actual);
    }
}
