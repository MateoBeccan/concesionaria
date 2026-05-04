package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.enumeration.EstadoTasacionUsado;
import com.concesionaria.app.repository.TasacionUsadoRepository;
import com.concesionaria.app.repository.MotorRepository;
import com.concesionaria.app.repository.TipoVehiculoRepository;
import com.concesionaria.app.repository.UserRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.repository.VersionRepository;
import com.concesionaria.app.service.dto.TasacionUsadoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.TasacionUsadoMapper;
import java.util.List;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TasacionUsadoServiceImplBusinessTest {

    @Mock private TasacionUsadoRepository tasacionUsadoRepository;
    @Mock private VentaRepository ventaRepository;
    @Mock private VersionRepository versionRepository;
    @Mock private MotorRepository motorRepository;
    @Mock private TipoVehiculoRepository tipoVehiculoRepository;
    @Mock private UserRepository userRepository;
    @Mock private TasacionUsadoMapper tasacionUsadoMapper;

    private TasacionUsadoServiceImpl service;

    @BeforeEach
    void setUp() {
        service =
            new TasacionUsadoServiceImpl(
                tasacionUsadoRepository,
                ventaRepository,
                versionRepository,
                motorRepository,
                tipoVehiculoRepository,
                userRepository,
                tasacionUsadoMapper
            );
    }

    @Test
    void devuelveSoloAceptadasDisponiblesPorCliente() {
        TasacionUsado entidad = new TasacionUsado();
        entidad.setId(200L);
        entidad.setEstado(EstadoTasacionUsado.ACEPTADA);
        TasacionUsadoDTO dto = new TasacionUsadoDTO();
        dto.setId(200L);
        dto.setEstado(EstadoTasacionUsado.ACEPTADA);

        when(tasacionUsadoRepository.findAceptadasDisponiblesByClienteId(15L)).thenReturn(List.of(entidad));
        when(tasacionUsadoMapper.toDto(entidad)).thenReturn(dto);

        List<TasacionUsadoDTO> result = service.findAceptadasDisponiblesByClienteId(15L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(200L);
    }

    @Test
    void exigeClienteParaBuscarDisponibles() {
        assertThrows(BadRequestException.class, () -> service.findAceptadasDisponiblesByClienteId(null));
    }

    @Test
    void noAceptaTasacionAceptadaSinVersion() {
        TasacionUsadoDTO dto = new TasacionUsadoDTO();
        dto.setEstado(EstadoTasacionUsado.ACEPTADA);
        dto.setMontoTasacion(BigDecimal.valueOf(1000));
        dto.setFechaTasacion(Instant.now());
        dto.setCliente(new com.concesionaria.app.service.dto.ClienteDTO());
        dto.getCliente().setId(1L);
        dto.setPatenteUsado("AB123CD");
        dto.setAnioUsado(2020);
        dto.setKmUsado(10000);
        dto.setColorUsado("Blanco");

        TasacionUsado entity = new TasacionUsado();
        entity.setEstado(EstadoTasacionUsado.ACEPTADA);
        entity.setMontoTasacion(BigDecimal.valueOf(1000));
        entity.setFechaTasacion(Instant.now());
        entity.setCliente(new com.concesionaria.app.domain.Cliente().id(1L));
        entity.setPatenteUsado("AB123CD");
        entity.setAnioUsado(2020);
        entity.setKmUsado(10000);
        entity.setColorUsado("Blanco");

        when(tasacionUsadoMapper.toEntity(dto)).thenReturn(entity);
        assertThrows(BadRequestException.class, () -> service.save(dto));
    }

    @Test
    void noAceptaTasacionAceptadaSinTasadorActivo() {
        TasacionUsadoDTO dto = new TasacionUsadoDTO();
        dto.setEstado(EstadoTasacionUsado.ACEPTADA);
        dto.setMontoTasacion(BigDecimal.valueOf(1000));
        dto.setFechaTasacion(Instant.now());
        dto.setCliente(new com.concesionaria.app.service.dto.ClienteDTO());
        dto.getCliente().setId(1L);
        dto.setPatenteUsado("AB123CD");
        dto.setAnioUsado(2020);
        dto.setKmUsado(10000);
        dto.setColorUsado("Blanco");

        TasacionUsado entity = new TasacionUsado();
        entity.setEstado(EstadoTasacionUsado.ACEPTADA);
        entity.setMontoTasacion(BigDecimal.valueOf(1000));
        entity.setFechaTasacion(Instant.now());
        entity.setCliente(new com.concesionaria.app.domain.Cliente().id(1L));
        entity.setPatenteUsado("AB123CD");
        entity.setAnioUsado(2020);
        entity.setKmUsado(10000);
        entity.setColorUsado("Blanco");
        entity.setVersion(new com.concesionaria.app.domain.Version().id(20L));
        entity.setTipoVehiculo(new com.concesionaria.app.domain.TipoVehiculo().id(2L));
        entity.setTasadorUser(new User());
        entity.getTasadorUser().setId(77L);

        when(tasacionUsadoMapper.toEntity(dto)).thenReturn(entity);
        when(versionRepository.existsById(20L)).thenReturn(true);
        when(tipoVehiculoRepository.existsById(2L)).thenReturn(true);
        User user = new User();
        user.setId(77L);
        user.setActivated(false);
        when(userRepository.findById(77L)).thenReturn(java.util.Optional.of(user));

        assertThrows(BadRequestException.class, () -> service.save(dto));
    }
}

