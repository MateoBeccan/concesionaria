package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Auto;
import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.service.dto.AutoDTO;
import com.concesionaria.app.service.dto.MarcaDTO;
import com.concesionaria.app.service.dto.ModeloDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Auto} and its DTO {@link AutoDTO}.
 */
@Mapper(componentModel = "spring")
public interface AutoMapper extends EntityMapper<AutoDTO, Auto> {
    @Mapping(target = "marca", source = "marca", qualifiedByName = "marcaId")
    @Mapping(target = "modelo", source = "modelo", qualifiedByName = "modeloId")
    @Mapping(target = "version", source = "version", qualifiedByName = "versionId")
    @Mapping(target = "motor", source = "motor", qualifiedByName = "motorId")
    @Mapping(target = "moneda", source = "moneda", qualifiedByName = "monedaId")
    AutoDTO toDto(Auto s);

    @Named("marcaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MarcaDTO toDtoMarcaId(Marca marca);

    @Named("modeloId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ModeloDTO toDtoModeloId(Modelo modelo);

    @Named("versionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VersionDTO toDtoVersionId(Version version);

    @Named("motorId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MotorDTO toDtoMotorId(Motor motor);

    @Named("monedaId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MonedaDTO toDtoMonedaId(Moneda moneda);
}
