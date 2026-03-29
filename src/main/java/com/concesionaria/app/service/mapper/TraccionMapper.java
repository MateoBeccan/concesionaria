package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Traccion;
import com.concesionaria.app.service.dto.TraccionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Traccion} and its DTO {@link TraccionDTO}.
 */
@Mapper(componentModel = "spring")
public interface TraccionMapper extends EntityMapper<TraccionDTO, Traccion> {}
