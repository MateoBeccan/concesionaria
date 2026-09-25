package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.ComprobanteCorrelativo;
import com.concesionaria.app.repository.ComprobanteCorrelativoRepository;
import com.concesionaria.app.service.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComprobanteNumeradorService {

    private final ComprobanteCorrelativoRepository comprobanteCorrelativoRepository;

    public ComprobanteNumeradorService(ComprobanteCorrelativoRepository comprobanteCorrelativoRepository) {
        this.comprobanteCorrelativoRepository = comprobanteCorrelativoRepository;
    }

    public Long siguienteNumero(Long tipoComprobanteId) {
        if (tipoComprobanteId == null) {
            throw new BadRequestException("Debe informar tipo de comprobante para numerar");
        }
        comprobanteCorrelativoRepository.asegurarFila(tipoComprobanteId);
        ComprobanteCorrelativo correlativo = comprobanteCorrelativoRepository
            .findByTipoComprobanteIdForUpdate(tipoComprobanteId)
            .orElseThrow(() -> new BadRequestException("No se pudo inicializar el correlativo del tipo de comprobante"));
        Long siguiente = (correlativo.getUltimoNumero() == null ? 0L : correlativo.getUltimoNumero()) + 1;
        correlativo.setUltimoNumero(siguiente);
        comprobanteCorrelativoRepository.save(correlativo);
        return siguiente;
    }
}
