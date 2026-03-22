package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Prueba1;
import com.concesionaria.app.service.dto.Prueba1DTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Prueba1} and its DTO {@link Prueba1DTO}.
 */
@Mapper(componentModel = "spring")
public interface Prueba1Mapper extends EntityMapper<Prueba1DTO, Prueba1> {}
