package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.repository.TipoComprobanteRepository;
import com.concesionaria.app.service.ComprobanteService;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PagoComprobanteBridge {

    private static final Logger LOG = LoggerFactory.getLogger(PagoComprobanteBridge.class);

    private final ComprobanteService comprobanteService;
    private final TipoComprobanteRepository tipoComprobanteRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final PagoMetodoPolicy pagoMetodoPolicy;

    public PagoComprobanteBridge(
        ComprobanteService comprobanteService,
        TipoComprobanteRepository tipoComprobanteRepository,
        ComprobanteRepository comprobanteRepository,
        PagoMetodoPolicy pagoMetodoPolicy
    ) {
        this.comprobanteService = comprobanteService;
        this.tipoComprobanteRepository = tipoComprobanteRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.pagoMetodoPolicy = pagoMetodoPolicy;
    }

    public void emitirSiCorresponde(Pago pago, MetodoPago metodoPago) {
        if (pago == null || pago.getId() == null || pago.getVenta() == null || pago.getVenta().getId() == null) {
            return;
        }
        if (!pagoMetodoPolicy.esMetodoEmitibleComprobante(metodoPago)) {
            return;
        }
        String codigoTipo = pagoMetodoPolicy.resolverTipoComprobantePago(metodoPago);
        TipoComprobante tipo = tipoComprobanteRepository.findByCodigoIgnoreCase(codigoTipo).orElse(null);
        if (tipo == null && !PagoMetodoPolicy.TIPO_COMPROBANTE_REC.equals(codigoTipo)) {
            tipo = tipoComprobanteRepository.findByCodigoIgnoreCase(PagoMetodoPolicy.TIPO_COMPROBANTE_REC).orElse(null);
        }
        if (tipo == null || tipo.getId() == null) {
            LOG.warn("No se emitio comprobante de pago para pagoId={} por falta de tipo comprobante REC/SEN", pago.getId());
            return;
        }
        if (comprobanteRepository.existsByPagoIdAndTipoComprobanteIdAndEstado(pago.getId(), tipo.getId(), EstadoComprobante.EMITIDO)) {
            return;
        }
        comprobanteService.emitirComprobantePago(pago.getId(), tipo.getId());
    }

    public void anularComprobantesAsociados(Pago pago, String motivo, String usuario, Instant fecha) {
        if (pago == null || pago.getId() == null) {
            return;
        }
        List<Comprobante> comprobantes = comprobanteRepository.findAllByPagoIdOrderByFechaEmisionDescIdDesc(pago.getId());
        for (Comprobante comprobante : comprobantes) {
            if (comprobante.getEstado() == EstadoComprobante.ANULADO) {
                continue;
            }
            comprobante.setEstado(EstadoComprobante.ANULADO);
            comprobante.setMotivoAnulacion("Anulado por anulacion de pago: " + motivo);
            comprobante.setUsuarioAnulacion(usuario);
            comprobante.setFechaAnulacion(fecha);
            comprobante.setLastModifiedBy(usuario);
            comprobante.setLastModifiedDate(fecha);
            comprobanteRepository.save(comprobante);
        }
    }
}
