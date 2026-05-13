package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.PlanAhorroDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlanAhorroService {
    PlanAhorroDTO save(PlanAhorroDTO dto);

    PlanAhorroDTO update(PlanAhorroDTO dto);

    Page<PlanAhorroDTO> findAll(Pageable pageable);

    Optional<PlanAhorroDTO> findOne(Long id);

    void delete(Long id);
}

