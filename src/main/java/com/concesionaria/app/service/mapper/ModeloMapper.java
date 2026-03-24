package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.service.dto.MarcaDTO;
import com.concesionaria.app.service.dto.ModeloDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Modelo} and its DTO {@link ModeloDTO}.
 */
@Mapper(componentModel = "spring")
public interface ModeloMapper extends EntityMapper<ModeloDTO, Modelo> {
    @Mapping(target = "marca", source = "marca")
    ModeloDTO toDto(Modelo s);

    @Named("marcaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MarcaDTO toDtoMarcaId(Marca marca);
}
