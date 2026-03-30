package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.service.dto.MarcaDTO;
import com.concesionaria.app.service.dto.ModeloDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Version} and its DTO {@link VersionDTO}.
 */
@Mapper(componentModel = "spring")
public interface VersionMapper extends EntityMapper<VersionDTO, Version> {

    @Mapping(target = "modelo", source = "modelo", qualifiedByName = "modeloBasic")
    VersionDTO toDto(Version s);

    @Named("modeloBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "marca", source = "marca", qualifiedByName = "marcaBasic")
    ModeloDTO toDtoModeloBasic(Modelo modelo);

    @Named("marcaBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    MarcaDTO toDtoMarcaBasic(Marca marca);
}
