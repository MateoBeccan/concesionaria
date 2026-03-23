package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.EstadoVenta;
import com.concesionaria.app.service.dto.EstadoVentaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link EstadoVenta} and its DTO {@link EstadoVentaDTO}.
 */
@Mapper(componentModel = "spring")
public interface EstadoVentaMapper extends EntityMapper<EstadoVentaDTO, EstadoVenta> {}
