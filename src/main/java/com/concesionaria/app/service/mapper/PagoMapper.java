package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.EntidadFinanciera;
import com.concesionaria.app.service.dto.EntidadFinancieraDTO;
import com.concesionaria.app.service.dto.MetodoPagoDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.dto.ReservaDTO;
import com.concesionaria.app.service.dto.TasacionUsadoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Pago} and its DTO {@link PagoDTO}.
 */
@Mapper(componentModel = "spring")
public interface PagoMapper extends EntityMapper<PagoDTO, Pago> {
    @Mapping(target = "venta", source = "venta", qualifiedByName = "ventaId")
    @Mapping(target = "reserva", source = "reserva", qualifiedByName = "reservaId")
    @Mapping(target = "metodoPago", source = "metodoPago", qualifiedByName = "metodoPagoResumen")
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaResumen")
    @Mapping(target = "entidadFinanciera", source = "entidadFinanciera", qualifiedByName = "entidadFinancieraResumen")
    @Mapping(target = "cotizacionId", source = "cotizacionRef.id")
    @Mapping(target = "tasacionUsadoId", source = "tasacionUsado.id")
    @Mapping(target = "tasacionUsado", source = "tasacionUsado", qualifiedByName = "tasacionUsadoResumen")
    PagoDTO toDto(Pago s);

    @Named("ventaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VentaDTO toDtoVentaId(Venta venta);

    @Named("reservaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "estado", source = "estado")
    ReservaDTO toDtoReservaId(Reserva reserva);

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

    @Named("entidadFinancieraResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "nombre", source = "nombre")
    EntidadFinancieraDTO toDtoEntidadFinancieraResumen(EntidadFinanciera entidadFinanciera);

    @Named("tasacionUsadoResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "estado", source = "estado")
    @Mapping(target = "montoTasacion", source = "montoTasacion")
    @Mapping(target = "marcaModeloUsado", source = "marcaModeloUsado")
    @Mapping(target = "patenteUsado", source = "patenteUsado")
    TasacionUsadoDTO toDtoTasacionUsadoResumen(TasacionUsado tasacionUsado);
}
