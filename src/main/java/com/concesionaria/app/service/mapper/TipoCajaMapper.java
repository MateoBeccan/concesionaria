package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.TipoCaja;
import com.concesionaria.app.service.dto.TipoCajaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TipoCaja} and its DTO {@link TipoCajaDTO}.
 */
@Mapper(componentModel = "spring")
public interface TipoCajaMapper extends EntityMapper<TipoCajaDTO, TipoCaja> {}
