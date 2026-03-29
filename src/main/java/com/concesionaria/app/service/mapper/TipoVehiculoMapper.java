package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.TipoVehiculo;
import com.concesionaria.app.service.dto.TipoVehiculoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TipoVehiculo} and its DTO {@link TipoVehiculoDTO}.
 */
@Mapper(componentModel = "spring")
public interface TipoVehiculoMapper extends EntityMapper<TipoVehiculoDTO, TipoVehiculo> {}
