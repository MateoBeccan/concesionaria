package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.service.dto.CuotaPlanAhorroDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CuotaPlanAhorroMapper extends EntityMapper<CuotaPlanAhorroDTO, CuotaPlanAhorro> {
    @Override
    @Mapping(target = "contratoId", source = "contrato.id")
    @Mapping(target = "pagoId", source = "pago.id")
    @Mapping(target = "comprobanteId", source = "comprobante.id")
    CuotaPlanAhorroDTO toDto(CuotaPlanAhorro entity);
}

