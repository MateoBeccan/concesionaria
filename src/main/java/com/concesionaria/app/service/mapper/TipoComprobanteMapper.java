package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.service.dto.TipoComprobanteDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TipoComprobante} and its DTO {@link TipoComprobanteDTO}.
 */
@Mapper(componentModel = "spring")
public interface TipoComprobanteMapper extends EntityMapper<TipoComprobanteDTO, TipoComprobante> {}
