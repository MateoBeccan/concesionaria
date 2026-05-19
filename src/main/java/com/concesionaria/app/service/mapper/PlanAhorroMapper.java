package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.domain.ReglaAdjudicacionPlan;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.PlanAhorroDTO;
import com.concesionaria.app.service.dto.ReglaAdjudicacionPlanDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PlanAhorroMapper extends EntityMapper<PlanAhorroDTO, PlanAhorro> {
    @Mapping(target = "versionObjetivo", source = "versionObjetivo", qualifiedByName = "versionIdNombre")
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaIdCodigo")
    @Mapping(target = "reglaAdjudicacion", source = "reglaAdjudicacion", qualifiedByName = "reglaIdNombre")
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

    @Named("reglaIdNombre")
    @Mapping(target = "descripcion", ignore = true)
    @Mapping(target = "tipoRegla", ignore = true)
    @Mapping(target = "minimoCuotas", ignore = true)
    @Mapping(target = "minimoPorcentaje", ignore = true)
    @Mapping(target = "permiteMora", ignore = true)
    @Mapping(target = "requiereContratoActivo", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    ReglaAdjudicacionPlanDTO toDtoReglaIdNombre(ReglaAdjudicacionPlan regla);
}

