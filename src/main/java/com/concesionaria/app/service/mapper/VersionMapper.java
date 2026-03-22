package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.service.dto.ModeloDTO;
import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Version} and its DTO {@link VersionDTO}.
 */
@Mapper(componentModel = "spring")
public interface VersionMapper extends EntityMapper<VersionDTO, Version> {
    @Mapping(target = "motoreses", source = "motoreses", qualifiedByName = "motorIdSet")
    @Mapping(target = "modeloses", source = "modeloses", qualifiedByName = "modeloIdSet")
    VersionDTO toDto(Version s);

    @Mapping(target = "removeMotores", ignore = true)
    @Mapping(target = "modeloses", ignore = true)
    @Mapping(target = "removeModelos", ignore = true)
    Version toEntity(VersionDTO versionDTO);

    @Named("motorId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MotorDTO toDtoMotorId(Motor motor);

    @Named("motorIdSet")
    default Set<MotorDTO> toDtoMotorIdSet(Set<Motor> motor) {
        return motor.stream().map(this::toDtoMotorId).collect(Collectors.toSet());
    }

    @Named("modeloId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ModeloDTO toDtoModeloId(Modelo modelo);

    @Named("modeloIdSet")
    default Set<ModeloDTO> toDtoModeloIdSet(Set<Modelo> modelo) {
        return modelo.stream().map(this::toDtoModeloId).collect(Collectors.toSet());
    }
}
