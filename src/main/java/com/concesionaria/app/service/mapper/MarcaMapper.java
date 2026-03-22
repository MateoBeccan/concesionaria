package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.service.dto.MarcaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Marca} and its DTO {@link MarcaDTO}.
 */
@Mapper(componentModel = "spring")
public interface MarcaMapper extends EntityMapper<MarcaDTO, Marca> {}
