package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.TipoVehiculo;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.InventarioDTO;
import com.concesionaria.app.service.dto.MarcaDTO;
import com.concesionaria.app.service.dto.ModeloDTO;
import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.dto.TasacionUsadoDTO;
import com.concesionaria.app.service.dto.TipoVehiculoDTO;
import com.concesionaria.app.service.dto.UserDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TasacionUsadoMapper extends EntityMapper<TasacionUsadoDTO, TasacionUsado> {

    @Mapping(target = "cliente", source = "cliente", qualifiedByName = "clienteResumen")
    @Mapping(target = "inventarioGenerado", source = "inventarioGenerado", qualifiedByName = "inventarioResumen")
    @Mapping(target = "version", source = "version", qualifiedByName = "versionResumen")
    @Mapping(target = "motor", source = "motor", qualifiedByName = "motorResumen")
    @Mapping(target = "tipoVehiculo", source = "tipoVehiculo", qualifiedByName = "tipoVehiculoResumen")
    @Mapping(target = "tasadorUser", source = "tasadorUser", qualifiedByName = "tasadorResumen")
    TasacionUsadoDTO toDto(TasacionUsado tasacionUsado);

    @Named("clienteResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "apellido", source = "apellido")
    ClienteDTO toDtoClienteResumen(Cliente cliente);

    @Named("inventarioResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "estadoInventario", source = "estadoInventario")
    InventarioDTO toDtoInventarioResumen(Inventario inventario);

    @Named("tasadorResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoTasadorResumen(User user);

    @Named("tipoVehiculoResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    TipoVehiculoDTO toDtoTipoVehiculoResumen(TipoVehiculo tipoVehiculo);

    @Named("motorResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    MotorDTO toDtoMotorResumen(Motor motor);

    @Named("versionResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "anioInicio", source = "anioInicio")
    @Mapping(target = "anioFin", source = "anioFin")
    @Mapping(target = "modelo", source = "modelo", qualifiedByName = "modeloResumen")
    VersionDTO toDtoVersionResumen(Version version);

    @Named("modeloResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "marca", source = "marca", qualifiedByName = "marcaResumen")
    ModeloDTO toDtoModeloResumen(Modelo modelo);

    @Named("marcaResumen")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    MarcaDTO toDtoMarcaResumen(Marca marca);
}
