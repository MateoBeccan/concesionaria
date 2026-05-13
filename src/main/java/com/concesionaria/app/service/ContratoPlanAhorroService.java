package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.ContratoPlanAhorroDTO;
import com.concesionaria.app.service.dto.CuotaPlanAhorroDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContratoPlanAhorroService {
    ContratoPlanAhorroDTO crearContrato(ContratoPlanAhorroDTO dto);

    Page<ContratoPlanAhorroDTO> findAll(Pageable pageable);

    Optional<ContratoPlanAhorroDTO> findOne(Long id);

    List<CuotaPlanAhorroDTO> findCuotas(Long contratoId);

    CuotaPlanAhorroDTO pagarCuota(Long cuotaId, BigDecimal monto, String observaciones);
}

