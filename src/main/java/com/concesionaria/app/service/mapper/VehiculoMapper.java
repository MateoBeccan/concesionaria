package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.domain.TipoVehiculo;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.dto.TipoVehiculoDTO;
import com.concesionaria.app.service.dto.VehiculoDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Vehiculo} and its DTO {@link VehiculoDTO}.
 */
@Mapper(componentModel = "spring")
public interface VehiculoMapper extends EntityMapper<VehiculoDTO, Vehiculo> {
    @Mapping(target = "version", source = "version", qualifiedByName = "versionId")
    @Mapping(target = "motor", source = "motor", qualifiedByName = "motorId")
    @Mapping(target = "tipoVehiculo", source = "tipoVehiculo", qualifiedByName = "tipoVehiculoId")
    VehiculoDTO toDto(Vehiculo s);

    @Named("versionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VersionDTO toDtoVersionId(Version version);

    @Named("motorId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MotorDTO toDtoMotorId(Motor motor);

    @Named("tipoVehiculoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TipoVehiculoDTO toDtoTipoVehiculoId(TipoVehiculo tipoVehiculo);
}
