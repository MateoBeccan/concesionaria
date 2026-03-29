package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.CarroceriaAsserts.*;
import static com.concesionaria.app.domain.CarroceriaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CarroceriaMapperTest {

    private CarroceriaMapper carroceriaMapper;

    @BeforeEach
    void setUp() {
        carroceriaMapper = new CarroceriaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCarroceriaSample1();
        var actual = carroceriaMapper.toEntity(carroceriaMapper.toDto(expected));
        assertCarroceriaAllPropertiesEquals(expected, actual);
    }
}
