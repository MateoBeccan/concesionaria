package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.TraccionAsserts.*;
import static com.concesionaria.app.domain.TraccionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TraccionMapperTest {

    private TraccionMapper traccionMapper;

    @BeforeEach
    void setUp() {
        traccionMapper = new TraccionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTraccionSample1();
        var actual = traccionMapper.toEntity(traccionMapper.toDto(expected));
        assertTraccionAllPropertiesEquals(expected, actual);
    }
}
