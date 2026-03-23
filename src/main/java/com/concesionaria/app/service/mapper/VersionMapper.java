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

    @Mapping(target = "modeloses", source = "modeloses", qualifiedByName = "modeloIdSet")
    @Mapping(target = "motoreses", source = "motoreses", qualifiedByName = "motorIdSet")
    VersionDTO toDto(Version s);

    @Mapping(target = "modeloses", ignore = true)
    @Mapping(target = "motoreses", ignore = true)
    Version toEntity(VersionDTO versionDTO);

    // ======================
    // MODELO
    // ======================

    @Named("modeloId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ModeloDTO toDtoModeloId(Modelo modelo);

    @Named("modeloIdSet")
    default Set<ModeloDTO> toDtoModeloIdSet(Set<Modelo> modelos) {
        return modelos.stream().map(this::toDtoModeloId).collect(Collectors.toSet());
    }

    // ======================
    // MOTOR
    // ======================

    @Named("motorId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MotorDTO toDtoMotorId(Motor motor);

    @Named("motorIdSet")
    default Set<MotorDTO> toDtoMotorIdSet(Set<Motor> motores) {
        return motores.stream().map(this::toDtoMotorId).collect(Collectors.toSet());
    }
}
