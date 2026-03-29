package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.CondicionIva;
import com.concesionaria.app.domain.TipoDocumento;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.CondicionIvaDTO;
import com.concesionaria.app.service.dto.TipoDocumentoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Cliente} and its DTO {@link ClienteDTO}.
 */
@Mapper(componentModel = "spring")
public interface ClienteMapper extends EntityMapper<ClienteDTO, Cliente> {
    @Mapping(target = "condicionIva", source = "condicionIva", qualifiedByName = "condicionIvaId")
    @Mapping(target = "tipoDocumento", source = "tipoDocumento", qualifiedByName = "tipoDocumentoId")
    ClienteDTO toDto(Cliente s);

    @Named("condicionIvaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CondicionIvaDTO toDtoCondicionIvaId(CondicionIva condicionIva);

    @Named("tipoDocumentoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TipoDocumentoDTO toDtoTipoDocumentoId(TipoDocumento tipoDocumento);
}
