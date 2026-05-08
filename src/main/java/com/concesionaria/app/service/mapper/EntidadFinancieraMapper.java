package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.EntidadFinanciera;
import com.concesionaria.app.service.dto.EntidadFinancieraDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EntidadFinancieraMapper extends EntityMapper<EntidadFinancieraDTO, EntidadFinanciera> {}
