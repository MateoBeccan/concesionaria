package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Combustible;
import com.concesionaria.app.service.dto.CombustibleDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Combustible} and its DTO {@link CombustibleDTO}.
 */
@Mapper(componentModel = "spring")
public interface CombustibleMapper extends EntityMapper<CombustibleDTO, Combustible> {}
