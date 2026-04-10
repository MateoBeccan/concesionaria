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

    @Mapping(target = "condicionIva", source = "condicionIva", qualifiedByName = "condicionIvaBasic")
    @Mapping(target = "tipoDocumento", source = "tipoDocumento", qualifiedByName = "tipoDocumentoBasic")
    ClienteDTO toDto(Cliente s);

    //  CONDICION IVA
    @Named("condicionIvaBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    CondicionIvaDTO toDtoCondicionIvaBasic(CondicionIva condicionIva);

    //  TIPO DOCUMENTO
    @Named("tipoDocumentoBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    TipoDocumentoDTO toDtoTipoDocumentoBasic(TipoDocumento tipoDocumento);
}
