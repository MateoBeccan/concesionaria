package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.InventarioDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.ReservaDTO;
import com.concesionaria.app.service.dto.VehiculoDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ReservaMapper extends EntityMapper<ReservaDTO, Reserva> {

    @Mapping(target = "inventario", source = "inventario", qualifiedByName = "inventarioResumen")
    @Mapping(target = "cliente", source = "cliente", qualifiedByName = "clienteResumen")
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaResumen")
    ReservaDTO toDto(Reserva reserva);

    @Named("inventarioResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "estadoInventario", source = "estadoInventario")
    @Mapping(target = "vehiculo", source = "vehiculo", qualifiedByName = "vehiculoResumen")
    InventarioDTO toDtoInventarioResumen(Inventario inventario);

    @Named("vehiculoResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "patente", source = "patente")
    VehiculoDTO toDtoVehiculoResumen(Vehiculo vehiculo);

    @Named("clienteResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "apellido", source = "apellido")
    ClienteDTO toDtoClienteResumen(Cliente cliente);

    @Named("monedaResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "simbolo", source = "simbolo")
    MonedaDTO toDtoMonedaResumen(Moneda moneda);
}
