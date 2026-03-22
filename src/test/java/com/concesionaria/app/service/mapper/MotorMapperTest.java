package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.MotorAsserts.*;
import static com.concesionaria.app.domain.MotorTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MotorMapperTest {

    private MotorMapper motorMapper;

    @BeforeEach
    void setUp() {
        motorMapper = new MotorMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMotorSample1();
        var actual = motorMapper.toEntity(motorMapper.toDto(expected));
        assertMotorAllPropertiesEquals(expected, actual);
    }
}
