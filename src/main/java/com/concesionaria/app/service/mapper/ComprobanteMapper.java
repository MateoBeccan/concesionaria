package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.service.dto.ComprobanteDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.TipoComprobanteDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Comprobante} and its DTO {@link ComprobanteDTO}.
 */
@Mapper(componentModel = "spring")
public interface ComprobanteMapper extends EntityMapper<ComprobanteDTO, Comprobante> {
    @Mapping(target = "venta", source = "venta", qualifiedByName = "ventaId")
    @Mapping(target = "tipoComprobante", source = "tipoComprobante", qualifiedByName = "tipoComprobanteResumen")
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaResumen")
    ComprobanteDTO toDto(Comprobante s);

    @Named("ventaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VentaDTO toDtoVentaId(Venta venta);

    @Named("tipoComprobanteResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "descripcion", source = "descripcion")
    TipoComprobanteDTO toDtoTipoComprobanteResumen(TipoComprobante tipoComprobante);

    @Named("monedaResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "simbolo", source = "simbolo")
    MonedaDTO toDtoMonedaResumen(Moneda moneda);
}
