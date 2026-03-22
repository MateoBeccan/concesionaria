package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.CombustibleAsserts.*;
import static com.concesionaria.app.domain.CombustibleTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CombustibleMapperTest {

    private CombustibleMapper combustibleMapper;

    @BeforeEach
    void setUp() {
        combustibleMapper = new CombustibleMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCombustibleSample1();
        var actual = combustibleMapper.toEntity(combustibleMapper.toDto(expected));
        assertCombustibleAllPropertiesEquals(expected, actual);
    }
}
