package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Auto;
import com.concesionaria.app.domain.ConfiguracionAuto;
import com.concesionaria.app.service.dto.AutoDTO;
import com.concesionaria.app.service.dto.ConfiguracionAutoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Auto} and its DTO {@link AutoDTO}.
 */
@Mapper(componentModel = "spring")
public interface AutoMapper extends EntityMapper<AutoDTO, Auto> {
    @Mapping(target = "configuracion", source = "configuracion", qualifiedByName = "configuracionAutoId")
    AutoDTO toDto(Auto s);

    @Named("configuracionAutoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ConfiguracionAutoDTO toDtoConfiguracionAutoId(ConfiguracionAuto configuracionAuto);
}
