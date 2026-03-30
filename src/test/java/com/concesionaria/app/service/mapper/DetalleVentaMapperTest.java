package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.DetalleVentaAsserts.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DetalleVentaMapperTest {

    private DetalleVentaMapper detalleVentaMapper;

    @BeforeEach
    void setUp() {
        detalleVentaMapper = new DetalleVentaMapperImpl();
    }

}
