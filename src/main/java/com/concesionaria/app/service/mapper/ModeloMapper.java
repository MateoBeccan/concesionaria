package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Carroceria;
import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.service.dto.CarroceriaDTO;
import com.concesionaria.app.service.dto.MarcaDTO;
import com.concesionaria.app.service.dto.ModeloDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ModeloMapper extends EntityMapper<ModeloDTO, Modelo> {

    @Mapping(target = "marca", source = "marca", qualifiedByName = "marcaBasic")
    @Mapping(target = "carroceria", source = "carroceria", qualifiedByName = "carroceriaBasic")
    ModeloDTO toDto(Modelo s);

    // MARCA COMPLETA
    @Named("marcaBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    MarcaDTO toDtoMarcaBasic(Marca marca);

    // CARROCERIA COMPLETA
    @Named("carroceriaBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    CarroceriaDTO toDtoCarroceriaBasic(Carroceria carroceria);
}
