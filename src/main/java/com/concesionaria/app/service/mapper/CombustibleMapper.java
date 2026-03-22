package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Combustible;
import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.service.dto.CombustibleDTO;
import com.concesionaria.app.service.dto.MotorDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Combustible} and its DTO {@link CombustibleDTO}.
 */
@Mapper(componentModel = "spring")
public interface CombustibleMapper extends EntityMapper<CombustibleDTO, Combustible> {
    @Mapping(target = "motor", source = "motor", qualifiedByName = "motorId")
    CombustibleDTO toDto(Combustible s);

    @Named("motorId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MotorDTO toDtoMotorId(Motor motor);
}
