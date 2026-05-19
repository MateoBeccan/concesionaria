package com.concesionaria.app.service.mapper;

import com.concesionaria.app.domain.ReglaAdjudicacionPlan;
import com.concesionaria.app.service.dto.ReglaAdjudicacionPlanDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReglaAdjudicacionPlanMapper extends EntityMapper<ReglaAdjudicacionPlanDTO, ReglaAdjudicacionPlan> {}
