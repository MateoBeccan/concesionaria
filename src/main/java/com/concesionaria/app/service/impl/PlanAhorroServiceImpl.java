package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.domain.ReglaAdjudicacionPlan;
import com.concesionaria.app.repository.PlanAhorroRepository;
import com.concesionaria.app.repository.ReglaAdjudicacionPlanRepository;
import com.concesionaria.app.service.PlanAhorroService;
import com.concesionaria.app.service.dto.PlanAhorroDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.PlanAhorroMapper;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlanAhorroServiceImpl implements PlanAhorroService {

    private final PlanAhorroRepository planAhorroRepository;
    private final ReglaAdjudicacionPlanRepository reglaRepository;
    private final PlanAhorroMapper planAhorroMapper;

    public PlanAhorroServiceImpl(
        PlanAhorroRepository planAhorroRepository,
        ReglaAdjudicacionPlanRepository reglaRepository,
        PlanAhorroMapper planAhorroMapper
    ) {
        this.planAhorroRepository = planAhorroRepository;
        this.reglaRepository = reglaRepository;
        this.planAhorroMapper = planAhorroMapper;
    }

    @Override
    public PlanAhorroDTO save(PlanAhorroDTO dto) {
        PlanAhorro entity = planAhorroMapper.toEntity(dto);
        validarReglaConCantidadCuotas(entity);
        asignarReglaDefaultSiCorresponde(entity);
        return planAhorroMapper.toDto(planAhorroRepository.save(entity));
    }

    @Override
    public PlanAhorroDTO update(PlanAhorroDTO dto) {
        PlanAhorro entity = planAhorroMapper.toEntity(dto);
        validarReglaConCantidadCuotas(entity);
        asignarReglaDefaultSiCorresponde(entity);
        return planAhorroMapper.toDto(planAhorroRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlanAhorroDTO> findAll(Pageable pageable) {
        return planAhorroRepository.findAll(pageable).map(planAhorroMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanAhorroDTO> findOne(Long id) {
        return planAhorroRepository.findById(id).map(planAhorroMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        planAhorroRepository.deleteById(id);
    }

    private void asignarReglaDefaultSiCorresponde(PlanAhorro plan) {
        if (plan.getReglaAdjudicacion() != null) {
            return;
        }
        ReglaAdjudicacionPlan defaultRule = reglaRepository
            .findByNombre("SIN_REGLA_DEFAULT")
            .orElseThrow(() -> new BadRequestException("No existe regla SIN_REGLA_DEFAULT para asignación automática"));
        plan.setReglaAdjudicacion(defaultRule);
    }

    private void validarReglaConCantidadCuotas(PlanAhorro plan) {
        if (plan.getReglaAdjudicacion() == null || plan.getReglaAdjudicacion().getMinimoCuotas() == null || plan.getCantidadCuotas() == null) {
            return;
        }
        if (plan.getReglaAdjudicacion().getMinimoCuotas() > plan.getCantidadCuotas()) {
            throw new BadRequestException("El mínimo de cuotas de la regla no puede superar la cantidad de cuotas del plan");
        }
    }
}

