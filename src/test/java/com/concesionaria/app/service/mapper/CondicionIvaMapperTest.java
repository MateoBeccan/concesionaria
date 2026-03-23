package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.CondicionIvaAsserts.*;
import static com.concesionaria.app.domain.CondicionIvaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CondicionIvaMapperTest {

    private CondicionIvaMapper condicionIvaMapper;

    @BeforeEach
    void setUp() {
        condicionIvaMapper = new CondicionIvaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCondicionIvaSample1();
        var actual = condicionIvaMapper.toEntity(condicionIvaMapper.toDto(expected));
        assertCondicionIvaAllPropertiesEquals(expected, actual);
    }
}
