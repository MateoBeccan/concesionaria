package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.service.dto.MetodoPagoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MetodoPago} and its DTO {@link MetodoPagoDTO}.
 */
@Mapper(componentModel = "spring")
public interface MetodoPagoMapper extends EntityMapper<MetodoPagoDTO, MetodoPago> {}
