package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Cotizacion;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.service.dto.CotizacionDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Cotizacion} and its DTO {@link CotizacionDTO}.
 */
@Mapper(componentModel = "spring")
public interface CotizacionMapper extends EntityMapper<CotizacionDTO, Cotizacion> {
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaId")
    CotizacionDTO toDto(Cotizacion s);

    @Named("monedaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MonedaDTO toDtoMonedaId(Moneda moneda);
}
