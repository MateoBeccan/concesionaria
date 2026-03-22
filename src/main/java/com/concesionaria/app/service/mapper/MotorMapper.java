package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Motor} and its DTO {@link MotorDTO}.
 */
@Mapper(componentModel = "spring")
public interface MotorMapper extends EntityMapper<MotorDTO, Motor> {
    @Mapping(target = "versioneses", source = "versioneses", qualifiedByName = "versionIdSet")
    MotorDTO toDto(Motor s);

    @Mapping(target = "versioneses", ignore = true)
    @Mapping(target = "removeVersiones", ignore = true)
    Motor toEntity(MotorDTO motorDTO);

    @Named("versionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VersionDTO toDtoVersionId(Version version);

    @Named("versionIdSet")
    default Set<VersionDTO> toDtoVersionIdSet(Set<Version> version) {
        return version.stream().map(this::toDtoVersionId).collect(Collectors.toSet());
    }
}
