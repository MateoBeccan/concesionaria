package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.CondicionIva;
import com.concesionaria.app.service.dto.CondicionIvaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CondicionIva} and its DTO {@link CondicionIvaDTO}.
 */
@Mapper(componentModel = "spring")
public interface CondicionIvaMapper extends EntityMapper<CondicionIvaDTO, CondicionIva> {}
