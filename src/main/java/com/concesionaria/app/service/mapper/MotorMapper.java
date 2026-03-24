package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Combustible;
import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.service.dto.CombustibleDTO;
import com.concesionaria.app.service.dto.MotorDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Motor} and its DTO {@link MotorDTO}.
 */
@Mapper(componentModel = "spring")
public interface MotorMapper extends EntityMapper<MotorDTO, Motor> {
    @Mapping(target = "combustible", source = "combustible", qualifiedByName = "combustibleId")
    MotorDTO toDto(Motor s);

    @Named("combustibleId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CombustibleDTO toDtoCombustibleId(Combustible combustible);
}
