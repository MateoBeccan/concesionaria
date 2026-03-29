package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Carroceria;
import com.concesionaria.app.service.dto.CarroceriaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Carroceria} and its DTO {@link CarroceriaDTO}.
 */
@Mapper(componentModel = "spring")
public interface CarroceriaMapper extends EntityMapper<CarroceriaDTO, Carroceria> {}
