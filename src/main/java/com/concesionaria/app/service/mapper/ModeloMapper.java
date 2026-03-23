package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.service.dto.MarcaDTO;
import com.concesionaria.app.service.dto.ModeloDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Modelo} and its DTO {@link ModeloDTO}.
 */
@Mapper(componentModel = "spring")
public interface ModeloMapper extends EntityMapper<ModeloDTO, Modelo> {
    @Mapping(target = "marca", source = "marca", qualifiedByName = "marcaId")
    @Mapping(target = "versioneses", source = "versioneses", qualifiedByName = "versionIdSet")
    ModeloDTO toDto(Modelo s);

    @Mapping(target = "versioneses", ignore = true)
    @Mapping(target = "removeVersiones", ignore = true)
    Modelo toEntity(ModeloDTO modeloDTO);

    @Named("marcaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MarcaDTO toDtoMarcaId(Marca marca);

    @Named("versionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VersionDTO toDtoVersionId(Version version);

    @Named("versionIdSet")
    default Set<VersionDTO> toDtoVersionIdSet(Set<Version> version) {
        return version.stream().map(this::toDtoVersionId).collect(Collectors.toSet());
    }
}
