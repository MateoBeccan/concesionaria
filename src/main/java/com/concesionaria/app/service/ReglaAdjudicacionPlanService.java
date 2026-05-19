package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.ReglaAdjudicacionPlanDTO;
import java.util.List;
import java.util.Optional;

public interface ReglaAdjudicacionPlanService {
    ReglaAdjudicacionPlanDTO save(ReglaAdjudicacionPlanDTO dto);

    ReglaAdjudicacionPlanDTO update(ReglaAdjudicacionPlanDTO dto);

    List<ReglaAdjudicacionPlanDTO> findAll(Boolean onlyActive);

    Optional<ReglaAdjudicacionPlanDTO> findOne(Long id);

    ReglaAdjudicacionPlanDTO deactivate(Long id);
}
