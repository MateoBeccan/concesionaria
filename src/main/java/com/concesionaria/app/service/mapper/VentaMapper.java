package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.UserDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VentaMapper extends EntityMapper<VentaDTO, Venta> {

    @Mapping(target = "cliente", source = "cliente", qualifiedByName = "clienteId")
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaId")
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    VentaDTO toDto(Venta s);

    @Named("clienteId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ClienteDTO toDtoClienteId(Cliente cliente);

    @Named("monedaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MonedaDTO toDtoMonedaId(Moneda moneda);

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
