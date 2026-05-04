package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.UbicacionStock;
import com.concesionaria.app.service.dto.UbicacionStockDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UbicacionStockMapper extends EntityMapper<UbicacionStockDTO, UbicacionStock> {}
