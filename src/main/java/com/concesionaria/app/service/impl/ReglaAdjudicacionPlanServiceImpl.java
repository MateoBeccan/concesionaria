package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.ReglaAdjudicacionPlan;
import com.concesionaria.app.domain.enumeration.TipoReglaAdjudicacionPlan;
import com.concesionaria.app.repository.ReglaAdjudicacionPlanRepository;
import com.concesionaria.app.service.ReglaAdjudicacionPlanService;
import com.concesionaria.app.service.dto.ReglaAdjudicacionPlanDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ReglaAdjudicacionPlanMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReglaAdjudicacionPlanServiceImpl implements ReglaAdjudicacionPlanService {

    private final ReglaAdjudicacionPlanRepository repository;
    private final ReglaAdjudicacionPlanMapper mapper;

    public ReglaAdjudicacionPlanServiceImpl(ReglaAdjudicacionPlanRepository repository, ReglaAdjudicacionPlanMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ReglaAdjudicacionPlanDTO save(ReglaAdjudicacionPlanDTO dto) {
        validarRegla(dto);
        ReglaAdjudicacionPlan entity = mapper.toEntity(dto);
        Instant now = Instant.now();
        entity.setCreatedDate(now);
        entity.setLastModifiedDate(now);
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public ReglaAdjudicacionPlanDTO update(ReglaAdjudicacionPlanDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestException("La regla a actualizar no tiene id");
        }
        validarRegla(dto);
        ReglaAdjudicacionPlan current = repository.findById(dto.getId()).orElseThrow(() -> new BadRequestException("La regla no existe"));
        ReglaAdjudicacionPlan entity = mapper.toEntity(dto);
        entity.setCreatedDate(current.getCreatedDate());
        entity.setLastModifiedDate(Instant.now());
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReglaAdjudicacionPlanDTO> findAll(Boolean onlyActive) {
        if (Boolean.TRUE.equals(onlyActive)) {
            return repository.findAllByActivoTrueOrderByNombreAsc().stream().map(mapper::toDto).toList();
        }
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReglaAdjudicacionPlanDTO> findOne(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public ReglaAdjudicacionPlanDTO deactivate(Long id) {
        ReglaAdjudicacionPlan regla = repository.findById(id).orElseThrow(() -> new BadRequestException("La regla no existe"));
        regla.setActivo(false);
        regla.setLastModifiedDate(Instant.now());
        return mapper.toDto(repository.save(regla));
    }

    private void validarRegla(ReglaAdjudicacionPlanDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new BadRequestException("El nombre de la regla es obligatorio");
        }
        if (dto.getTipoRegla() == null) {
            throw new BadRequestException("El tipo de regla es obligatorio");
        }
        TipoReglaAdjudicacionPlan tipo = dto.getTipoRegla();
        Integer minimoCuotas = dto.getMinimoCuotas();
        BigDecimal minimoPorcentaje = dto.getMinimoPorcentaje();

        boolean requiereCuotas = tipo == TipoReglaAdjudicacionPlan.POR_CUOTAS || tipo == TipoReglaAdjudicacionPlan.CUOTAS_O_PORCENTAJE || tipo == TipoReglaAdjudicacionPlan.CUOTAS_Y_PORCENTAJE;
        boolean requierePorcentaje =
            tipo == TipoReglaAdjudicacionPlan.POR_PORCENTAJE || tipo == TipoReglaAdjudicacionPlan.CUOTAS_O_PORCENTAJE || tipo == TipoReglaAdjudicacionPlan.CUOTAS_Y_PORCENTAJE;

        if (requiereCuotas && (minimoCuotas == null || minimoCuotas <= 0)) {
            throw new BadRequestException("El tipo de regla exige minimo de cuotas mayor a 0");
        }
        if (requierePorcentaje) {
            if (minimoPorcentaje == null || minimoPorcentaje.compareTo(BigDecimal.ZERO) <= 0 || minimoPorcentaje.compareTo(new BigDecimal("100")) > 0) {
                throw new BadRequestException("El minimo de porcentaje debe ser mayor a 0 y menor o igual a 100");
            }
        }
    }
}
