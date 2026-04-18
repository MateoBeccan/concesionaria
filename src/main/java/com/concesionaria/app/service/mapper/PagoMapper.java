package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.service.dto.MetodoPagoDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Pago} and its DTO {@link PagoDTO}.
 */
@Mapper(componentModel = "spring")
public interface PagoMapper extends EntityMapper<PagoDTO, Pago> {
    @Mapping(target = "venta", source = "venta", qualifiedByName = "ventaId")
    @Mapping(target = "metodoPago", source = "metodoPago", qualifiedByName = "metodoPagoResumen")
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaResumen")
    PagoDTO toDto(Pago s);

    @Named("ventaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VentaDTO toDtoVentaId(Venta venta);

    @Named("metodoPagoResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "descripcion", source = "descripcion")
    MetodoPagoDTO toDtoMetodoPagoResumen(MetodoPago metodoPago);

    @Named("monedaResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "simbolo", source = "simbolo")
    MonedaDTO toDtoMonedaResumen(Moneda moneda);
}
