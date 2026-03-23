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
    @Mapping(target = "tipoComprobante", source = "tipoComprobante", qualifiedByName = "tipoComprobanteId")
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaId")
    ComprobanteDTO toDto(Comprobante s);

    @Named("ventaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VentaDTO toDtoVentaId(Venta venta);

    @Named("tipoComprobanteId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TipoComprobanteDTO toDtoTipoComprobanteId(TipoComprobante tipoComprobante);

    @Named("monedaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MonedaDTO toDtoMonedaId(Moneda moneda);
}
