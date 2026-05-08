package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.EntidadFinanciera;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.MovimientoCaja;
import com.concesionaria.app.service.dto.EntidadFinancieraDTO;
import com.concesionaria.app.service.dto.MetodoPagoDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.MovimientoCajaDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface MovimientoCajaMapper extends EntityMapper<MovimientoCajaDTO, MovimientoCaja> {
    @Override
    @Mapping(target = "pagoId", source = "pago.id")
    @Mapping(target = "ventaId", source = "venta.id")
    @Mapping(target = "reservaId", source = "reserva.id")
    @Mapping(target = "metodoPago", source = "metodoPago", qualifiedByName = "metodoPagoResumen")
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaResumen")
    @Mapping(target = "entidadFinanciera", source = "entidadFinanciera", qualifiedByName = "entidadResumen")
    MovimientoCajaDTO toDto(MovimientoCaja entity);

    @Named("metodoPagoResumen")
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "requiereReferencia", ignore = true)
    MetodoPagoDTO toMetodoPagoResumen(MetodoPago metodoPago);

    @Named("monedaResumen")
    @Mapping(target = "activo", ignore = true)
    MonedaDTO toMonedaResumen(Moneda moneda);

    @Named("entidadResumen")
    @Mapping(target = "descripcion", ignore = true)
    @Mapping(target = "activa", ignore = true)
    EntidadFinancieraDTO toEntidadResumen(EntidadFinanciera entidadFinanciera);
}
