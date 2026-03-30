package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.*;
import com.concesionaria.app.service.dto.*;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Vehiculo} and its DTO {@link VehiculoDTO}.
 */
@Mapper(componentModel = "spring")
public interface VehiculoMapper extends EntityMapper<VehiculoDTO, Vehiculo> {

    @Mapping(target = "version", source = "version", qualifiedByName = "versionBasic")
    @Mapping(target = "motor", source = "motor", qualifiedByName = "motorBasic")
    @Mapping(target = "tipoVehiculo", source = "tipoVehiculo", qualifiedByName = "tipoVehiculoBasic")
    VehiculoDTO toDto(Vehiculo s);

    // VERSION
    @Named("versionBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "modelo", source = "modelo", qualifiedByName = "modeloBasic")
    VersionDTO toDtoVersionBasic(Version version);

    // MOTOR
    @Named("motorBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "potenciaHp", source = "potenciaHp")
    @Mapping(target = "cilindradaCc", source = "cilindradaCc")
    MotorDTO toDtoMotorBasic(Motor motor);

    // TIPO VEHICULO
    @Named("tipoVehiculoBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    TipoVehiculoDTO toDtoTipoVehiculoBasic(TipoVehiculo tipoVehiculo);

    @Named("modeloBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "marca", source = "marca", qualifiedByName = "marcaBasic")
    ModeloDTO toDtoModeloBasic(Modelo modelo);

    @Named("marcaBasic")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    MarcaDTO toDtoMarcaBasic(Marca marca);


}
