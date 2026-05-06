package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.ComprobantePdfResult;
import java.util.Optional;

public interface PdfComprobanteService {
    Optional<ComprobantePdfResult> generarPdfComprobante(Long comprobanteId);
}

