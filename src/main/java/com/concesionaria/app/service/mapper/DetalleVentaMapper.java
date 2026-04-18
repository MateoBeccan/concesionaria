package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.DetalleVenta;
import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.service.dto.DetalleVentaDTO;
import com.concesionaria.app.service.dto.MarcaDTO;
import com.concesionaria.app.service.dto.ModeloDTO;
import com.concesionaria.app.service.dto.VehiculoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DetalleVenta} and its DTO {@link DetalleVentaDTO}.
 */
@Mapper(componentModel = "spring")
public interface DetalleVentaMapper extends EntityMapper<DetalleVentaDTO, DetalleVenta> {
    @Mapping(target = "venta", source = "venta", qualifiedByName = "ventaId")
    @Mapping(target = "vehiculo", source = "vehiculo", qualifiedByName = "vehiculoResumen")
    DetalleVentaDTO toDto(DetalleVenta s);

    @Named("ventaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VentaDTO toDtoVentaId(Venta venta);

    @Named("vehiculoResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "patente", source = "patente")
    @Mapping(target = "estado", source = "estado")
    @Mapping(target = "condicion", source = "condicion")
    @Mapping(target = "estadoInventario", source = "inventario.estadoInventario")
    @Mapping(target = "precio", source = "precio")
    @Mapping(target = "version", source = "version", qualifiedByName = "versionResumen")
    VehiculoDTO toDtoVehiculoResumen(Vehiculo vehiculo);

    @Named("versionResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "modelo", source = "modelo", qualifiedByName = "modeloResumen")
    VersionDTO toDtoVersionResumen(Version version);

    @Named("modeloResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "marca", source = "marca", qualifiedByName = "marcaResumen")
    ModeloDTO toDtoModeloResumen(Modelo modelo);

    @Named("marcaResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    MarcaDTO toDtoMarcaResumen(Marca marca);
}
