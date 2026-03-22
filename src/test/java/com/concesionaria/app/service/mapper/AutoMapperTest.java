package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.AutoAsserts.*;
import static com.concesionaria.app.domain.AutoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutoMapperTest {

    private AutoMapper autoMapper;

    @BeforeEach
    void setUp() {
        autoMapper = new AutoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAutoSample1();
        var actual = autoMapper.toEntity(autoMapper.toDto(expected));
        assertAutoAllPropertiesEquals(expected, actual);
    }
}
