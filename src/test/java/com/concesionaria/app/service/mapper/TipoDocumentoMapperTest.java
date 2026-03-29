package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.TipoDocumentoAsserts.*;
import static com.concesionaria.app.domain.TipoDocumentoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TipoDocumentoMapperTest {

    private TipoDocumentoMapper tipoDocumentoMapper;

    @BeforeEach
    void setUp() {
        tipoDocumentoMapper = new TipoDocumentoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTipoDocumentoSample1();
        var actual = tipoDocumentoMapper.toEntity(tipoDocumentoMapper.toDto(expected));
        assertTipoDocumentoAllPropertiesEquals(expected, actual);
    }
}
