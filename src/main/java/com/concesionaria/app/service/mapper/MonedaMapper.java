package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.service.dto.MonedaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Moneda} and its DTO {@link MonedaDTO}.
 */
@Mapper(componentModel = "spring")
public interface MonedaMapper extends EntityMapper<MonedaDTO, Moneda> {}
