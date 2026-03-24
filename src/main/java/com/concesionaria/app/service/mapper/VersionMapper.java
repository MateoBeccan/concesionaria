package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Version;
import com.concesionaria.app.service.dto.VersionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Version} and its DTO {@link VersionDTO}.
 */
@Mapper(componentModel = "spring")
public interface VersionMapper extends EntityMapper<VersionDTO, Version> {}
