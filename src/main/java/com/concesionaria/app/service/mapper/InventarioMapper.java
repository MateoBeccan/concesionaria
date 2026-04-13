package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.InventarioDTO;
import com.concesionaria.app.service.dto.VehiculoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Inventario} and its DTO {@link InventarioDTO}.
 */
@Mapper(componentModel = "spring")
public interface InventarioMapper extends EntityMapper<InventarioDTO, Inventario> {

    @Mapping(target = "vehiculo", source = "vehiculo", qualifiedByName = "vehiculoId")
    @Mapping(target = "clienteReserva", source = "clienteReserva", qualifiedByName = "clienteId")
    InventarioDTO toDto(Inventario s);

    @Named("vehiculoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "patente", source = "patente")
    @Mapping(target = "condicion", source = "condicion")
    @Mapping(target = "estado", source = "estado")
    VehiculoDTO toDtoVehiculoId(Vehiculo vehiculo);

    @Named("clienteId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "apellido", source = "apellido")
    ClienteDTO toDtoClienteId(Cliente cliente);

    // 🔥 clave
    @Override
    @Mapping(target = "vehiculo.inventario", ignore = true)
    Inventario toEntity(InventarioDTO dto);
}
