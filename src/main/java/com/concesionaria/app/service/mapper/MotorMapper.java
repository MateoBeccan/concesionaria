package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Combustible;
import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.domain.TipoCaja;
import com.concesionaria.app.domain.Traccion;
import com.concesionaria.app.service.dto.CombustibleDTO;
import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.dto.TipoCajaDTO;
import com.concesionaria.app.service.dto.TraccionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Motor} and its DTO {@link MotorDTO}.
 */
@Mapper(componentModel = "spring")
public interface MotorMapper extends EntityMapper<MotorDTO, Motor> {
    @Mapping(target = "combustible", source = "combustible", qualifiedByName = "combustibleId")
    @Mapping(target = "tipoCaja", source = "tipoCaja", qualifiedByName = "tipoCajaId")
    @Mapping(target = "traccion", source = "traccion", qualifiedByName = "traccionId")
    MotorDTO toDto(Motor s);

    @Named("combustibleId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CombustibleDTO toDtoCombustibleId(Combustible combustible);

    @Named("tipoCajaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TipoCajaDTO toDtoTipoCajaId(TipoCaja tipoCaja);

    @Named("traccionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TraccionDTO toDtoTraccionId(Traccion traccion);
}
