package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.AdjudicacionPlanAhorroDTO;
import com.concesionaria.app.service.dto.InventarioDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdjudicacionPlanAhorroService {
    AdjudicacionPlanAhorroDTO adjudicarContrato(Long contratoId, String observaciones);

    AdjudicacionPlanAhorroDTO asignarInventario(Long adjudicacionId, Long inventarioId);

    AdjudicacionPlanAhorroDTO generarVenta(Long adjudicacionId);

    AdjudicacionPlanAhorroDTO cancelarAdjudicacion(Long adjudicacionId, String motivo);

    AdjudicacionPlanAhorroDTO marcarEntregada(Long adjudicacionId);

    Optional<AdjudicacionPlanAhorroDTO> findLatestByContrato(Long contratoId);

    List<InventarioDTO> findInventarioCompatibleDisponible(Long contratoId);

    Page<AdjudicacionPlanAhorroDTO> findAll(Pageable pageable);

    Page<AdjudicacionPlanAhorroDTO> findAllCurrentUser(Pageable pageable);
}
