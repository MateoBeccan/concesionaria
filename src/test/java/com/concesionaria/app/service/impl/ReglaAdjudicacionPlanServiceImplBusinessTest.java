package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.ReglaAdjudicacionPlan;
import com.concesionaria.app.domain.enumeration.TipoReglaAdjudicacionPlan;
import com.concesionaria.app.repository.ReglaAdjudicacionPlanRepository;
import com.concesionaria.app.service.dto.ReglaAdjudicacionPlanDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ReglaAdjudicacionPlanMapperImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReglaAdjudicacionPlanServiceImplBusinessTest {

    @Mock
    private ReglaAdjudicacionPlanRepository repository;

    private ReglaAdjudicacionPlanServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReglaAdjudicacionPlanServiceImpl(repository, new ReglaAdjudicacionPlanMapperImpl());
    }

    @Test
    void crearReglaPorCuotasValida() {
        when(repository.save(any(ReglaAdjudicacionPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReglaAdjudicacionPlanDTO dto = new ReglaAdjudicacionPlanDTO();
        dto.setNombre("R1");
        dto.setTipoRegla(TipoReglaAdjudicacionPlan.POR_CUOTAS);
        dto.setMinimoCuotas(12);
        dto.setPermiteMora(false);
        dto.setRequiereContratoActivo(true);
        dto.setActivo(true);
        ReglaAdjudicacionPlanDTO result = service.save(dto);
        assertThat(result.getNombre()).isEqualTo("R1");
    }

    @Test
    void bloquearPorCuotasSinMinimo() {
        ReglaAdjudicacionPlanDTO dto = new ReglaAdjudicacionPlanDTO();
        dto.setNombre("R2");
        dto.setTipoRegla(TipoReglaAdjudicacionPlan.POR_CUOTAS);
        dto.setPermiteMora(false);
        dto.setRequiereContratoActivo(true);
        dto.setActivo(true);
        assertThrows(BadRequestException.class, () -> service.save(dto));
    }

    @Test
    void bloquearPorPorcentajeMayorACien() {
        ReglaAdjudicacionPlanDTO dto = new ReglaAdjudicacionPlanDTO();
        dto.setNombre("R3");
        dto.setTipoRegla(TipoReglaAdjudicacionPlan.POR_PORCENTAJE);
        dto.setMinimoPorcentaje(new BigDecimal("120.00"));
        dto.setPermiteMora(false);
        dto.setRequiereContratoActivo(true);
        dto.setActivo(true);
        assertThrows(BadRequestException.class, () -> service.save(dto));
    }

    @Test
    void permitirSinReglaSinMinimos() {
        when(repository.save(any(ReglaAdjudicacionPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReglaAdjudicacionPlanDTO dto = new ReglaAdjudicacionPlanDTO();
        dto.setNombre("R4");
        dto.setTipoRegla(TipoReglaAdjudicacionPlan.SIN_REGLA);
        dto.setPermiteMora(true);
        dto.setRequiereContratoActivo(false);
        dto.setActivo(true);
        ReglaAdjudicacionPlanDTO result = service.save(dto);
        assertThat(result.getTipoRegla()).isEqualTo(TipoReglaAdjudicacionPlan.SIN_REGLA);
    }

    @Test
    void desactivarRegla() {
        ReglaAdjudicacionPlan entity = new ReglaAdjudicacionPlan();
        entity.setId(1L);
        entity.setNombre("R5");
        entity.setTipoRegla(TipoReglaAdjudicacionPlan.MANUAL);
        entity.setActivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(ReglaAdjudicacionPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReglaAdjudicacionPlanDTO result = service.deactivate(1L);
        assertThat(result.getActivo()).isFalse();
    }
}
