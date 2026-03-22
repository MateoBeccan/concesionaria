package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.Prueba1Asserts.*;
import static com.concesionaria.app.domain.Prueba1TestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Prueba1MapperTest {

    private Prueba1Mapper prueba1Mapper;

    @BeforeEach
    void setUp() {
        prueba1Mapper = new Prueba1MapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPrueba1Sample1();
        var actual = prueba1Mapper.toEntity(prueba1Mapper.toDto(expected));
        assertPrueba1AllPropertiesEquals(expected, actual);
    }
}
