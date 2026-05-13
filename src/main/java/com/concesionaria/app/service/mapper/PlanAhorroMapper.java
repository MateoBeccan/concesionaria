package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.PlanAhorroDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PlanAhorroMapper extends EntityMapper<PlanAhorroDTO, PlanAhorro> {
    @Mapping(target = "versionObjetivo", source = "versionObjetivo", qualifiedByName = "versionIdNombre")
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaIdCodigo")
    PlanAhorroDTO toDto(PlanAhorro s);

    @Named("versionIdNombre")
    @Mapping(target = "modelo", ignore = true)
    @Mapping(target = "descripcion", ignore = true)
    @Mapping(target = "anioInicio", ignore = true)
    @Mapping(target = "anioFin", ignore = true)
    VersionDTO toDtoVersionIdNombre(Version version);

    @Named("monedaIdCodigo")
    @Mapping(target = "descripcion", ignore = true)
    @Mapping(target = "simbolo", ignore = true)
    @Mapping(target = "activo", ignore = true)
    MonedaDTO toDtoMonedaIdCodigo(Moneda moneda);
}

