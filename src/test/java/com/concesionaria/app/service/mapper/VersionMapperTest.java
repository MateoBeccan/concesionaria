package com.concesionaria.app.service.mapper;

import static com.concesionaria.app.domain.VersionAsserts.*;
import static com.concesionaria.app.domain.VersionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VersionMapperTest {

    private VersionMapper versionMapper;

    @BeforeEach
    void setUp() {
        versionMapper = new VersionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getVersionSample1();
        var actual = versionMapper.toEntity(versionMapper.toDto(expected));
        assertVersionAllPropertiesEquals(expected, actual);
    }
}
