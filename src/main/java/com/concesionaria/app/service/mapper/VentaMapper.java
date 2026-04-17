package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.CondicionIva;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.CondicionIvaDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.UserDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VentaMapper extends EntityMapper<VentaDTO, Venta> {

    @Mapping(target = "cliente", source = "cliente", qualifiedByName = "clienteResumen")
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaResumen")
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    VentaDTO toDto(Venta s);

    @Named("clienteResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "apellido", source = "apellido")
    @Mapping(target = "nroDocumento", source = "nroDocumento")
    @Mapping(target = "telefono", source = "telefono")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "condicionIva", source = "condicionIva", qualifiedByName = "condicionIvaResumen")
    ClienteDTO toDtoClienteResumen(Cliente cliente);

    @Named("monedaResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "simbolo", source = "simbolo")
    MonedaDTO toDtoMonedaResumen(Moneda moneda);

    @Named("condicionIvaResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "descripcion", source = "descripcion")
    CondicionIvaDTO toDtoCondicionIvaResumen(CondicionIva condicionIva);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    // IMPORTANTE: ignorar campos problemáticos
    @Override
    @Mapping(target = "user", ignore = true)
    Venta toEntity(VentaDTO dto);
}
