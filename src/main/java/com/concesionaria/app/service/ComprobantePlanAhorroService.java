package com.concesionaria.app.service;

import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.service.dto.ComprobantePdfResult;
import com.concesionaria.app.service.dto.ComprobantePlanAhorroDTO;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ComprobantePlanAhorroService {
    ComprobantePlanAhorroDTO emitirParaCuota(CuotaPlanAhorro cuota, Pago pago);

    Optional<ComprobantePlanAhorroDTO> findOne(Long id);

    List<ComprobantePlanAhorroDTO> findByCuota(Long cuotaId);

    Optional<ComprobantePdfResult> generarPdf(Long id);

    ComprobantePlanAhorroDTO anular(Long id, String motivo);

    void anularPorPago(Long pagoId, String motivo, String usuario, Instant fecha);

    void delete(Long id);
}

