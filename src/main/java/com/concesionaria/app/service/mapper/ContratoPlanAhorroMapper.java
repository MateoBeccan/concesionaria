package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.ContratoPlanAhorroDTO;
import com.concesionaria.app.service.dto.PlanAhorroDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = { PlanAhorroMapper.class })
public interface ContratoPlanAhorroMapper extends EntityMapper<ContratoPlanAhorroDTO, ContratoPlanAhorro> {
    @Mapping(target = "cliente", source = "cliente", qualifiedByName = "clienteBasico")
    @Mapping(target = "plan", source = "plan")
    ContratoPlanAhorroDTO toDto(ContratoPlanAhorro s);

    @Named("clienteBasico")
    @Mapping(target = "telefono", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "direccion", ignore = true)
    @Mapping(target = "ciudad", ignore = true)
    @Mapping(target = "provincia", ignore = true)
    @Mapping(target = "pais", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaAlta", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "condicionIva", ignore = true)
    @Mapping(target = "tipoDocumento", ignore = true)
    @Mapping(target = "tipoPersona", ignore = true)
    ClienteDTO toDtoClienteBasico(Cliente cliente);

    @Named("planBasico")
    @Mapping(target = "descripcion", ignore = true)
    @Mapping(target = "versionObjetivo", ignore = true)
    @Mapping(target = "cantidadCuotas", ignore = true)
    @Mapping(target = "valorMovil", ignore = true)
    @Mapping(target = "moneda", ignore = true)
    @Mapping(target = "estado", ignore = true)
    PlanAhorroDTO toDtoPlanBasico(PlanAhorro planAhorro);
}

