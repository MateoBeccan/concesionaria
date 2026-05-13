package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.repository.PlanAhorroRepository;
import com.concesionaria.app.service.PlanAhorroService;
import com.concesionaria.app.service.dto.PlanAhorroDTO;
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
    private final PlanAhorroMapper planAhorroMapper;

    public PlanAhorroServiceImpl(PlanAhorroRepository planAhorroRepository, PlanAhorroMapper planAhorroMapper) {
        this.planAhorroRepository = planAhorroRepository;
        this.planAhorroMapper = planAhorroMapper;
    }

    @Override
    public PlanAhorroDTO save(PlanAhorroDTO dto) {
        PlanAhorro entity = planAhorroMapper.toEntity(dto);
        return planAhorroMapper.toDto(planAhorroRepository.save(entity));
    }

    @Override
    public PlanAhorroDTO update(PlanAhorroDTO dto) {
        PlanAhorro entity = planAhorroMapper.toEntity(dto);
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
}

