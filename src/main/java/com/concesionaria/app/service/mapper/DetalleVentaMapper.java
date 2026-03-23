package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Auto;
import com.concesionaria.app.domain.DetalleVenta;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.service.dto.AutoDTO;
import com.concesionaria.app.service.dto.DetalleVentaDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DetalleVenta} and its DTO {@link DetalleVentaDTO}.
 */
@Mapper(componentModel = "spring")
public interface DetalleVentaMapper extends EntityMapper<DetalleVentaDTO, DetalleVenta> {
    @Mapping(target = "venta", source = "venta", qualifiedByName = "ventaId")
    @Mapping(target = "auto", source = "auto", qualifiedByName = "autoId")
    DetalleVentaDTO toDto(DetalleVenta s);

    @Named("ventaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VentaDTO toDtoVentaId(Venta venta);

    @Named("autoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AutoDTO toDtoAutoId(Auto auto);
}
