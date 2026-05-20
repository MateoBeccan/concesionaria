package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.EntidadFinanciera;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.repository.EntidadFinancieraRepository;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PagoMetodoPolicy {

    private static final Logger LOG = LoggerFactory.getLogger(PagoMetodoPolicy.class);

    public static final String CODIGO_CONTADO = "CONTADO";
    public static final String CODIGO_TRANSFERENCIA = "TRANSFERENCIA";
    public static final String CODIGO_DEPOSITO = "DEPOSITO";
    public static final String CODIGO_CHEQUE = "CHEQUE";
    public static final String CODIGO_DEBITO = "DEBITO";
    public static final String CODIGO_CREDITO = "CREDITO";
    public static final String CODIGO_AJUSTE = "AJUSTE";
    public static final String CODIGO_BONIFICACION = "BONIFICACION";
    public static final String CODIGO_ENTREGA_USADO = "ENTREGA_USADO";
    public static final String CODIGO_PLAN_AHORRO = "PLAN_AHORRO";
    public static final String CODIGO_TARJETA = "TARJETA";
    public static final String CODIGO_SENIA = "SENIA";
    public static final String TIPO_COMPROBANTE_REC = "REC";
    public static final String TIPO_COMPROBANTE_SEN = "SEN";

    private final EntidadFinancieraRepository entidadFinancieraRepository;
    private final PagoTextNormalizer pagoTextNormalizer;

    public PagoMetodoPolicy(EntidadFinancieraRepository entidadFinancieraRepository, PagoTextNormalizer pagoTextNormalizer) {
        this.entidadFinancieraRepository = entidadFinancieraRepository;
        this.pagoTextNormalizer = pagoTextNormalizer;
    }

    public void validarMetodoPagoEspecial(MetodoPago metodoPago) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        if (codigoMetodo == null) {
            return;
        }
        if (
            (CODIGO_TRANSFERENCIA.equals(codigoMetodo) ||
                CODIGO_DEPOSITO.equals(codigoMetodo) ||
                CODIGO_CHEQUE.equals(codigoMetodo) ||
                CODIGO_TARJETA.equals(codigoMetodo) ||
                CODIGO_DEBITO.equals(codigoMetodo) ||
                CODIGO_CREDITO.equals(codigoMetodo)) &&
            (metodoPago.getRequiereReferencia() == null || !metodoPago.getRequiereReferencia())
        ) {
            LOG.warn("Metodo de pago {} deberia requerir referencia para trazabilidad", codigoMetodo);
        }
    }

    public void validarDatosOperacionParaMetodo(PagoDTO pagoDTO, MetodoPago metodoPago) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        if (codigoMetodo == null) {
            return;
        }
        if (CODIGO_CONTADO.equals(codigoMetodo)) {
            return;
        }
        // Referencia y numero de operacion se autogeneran en backend cuando no son informados.
    }

    public void validarDatosAdministrativosPorMetodo(PagoDTO pagoDTO, MetodoPago metodoPago) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        if (codigoMetodo == null) {
            return;
        }
        if (CODIGO_PLAN_AHORRO.equals(codigoMetodo)) {
            return;
        }
        boolean bancoVacio = pagoDTO.getBancoEntidad() == null || pagoDTO.getBancoEntidad().isBlank();
        boolean entidadVacia = pagoDTO.getEntidadFinanciera() == null || pagoDTO.getEntidadFinanciera().getId() == null;
        boolean sinIdentificacionEntidad = bancoVacio && entidadVacia;
        boolean comprobanteVacio = pagoDTO.getComprobanteExterno() == null || pagoDTO.getComprobanteExterno().isBlank();
        boolean observacionesVacio = pagoDTO.getObservaciones() == null || pagoDTO.getObservaciones().isBlank();

        switch (codigoMetodo) {
            case CODIGO_TRANSFERENCIA:
            case CODIGO_DEPOSITO:
                if (sinIdentificacionEntidad) {
                    throw new BadRequestException("Para " + codigoMetodo + " debe informar entidad financiera");
                }
                break;
            case CODIGO_CHEQUE:
                if (sinIdentificacionEntidad) {
                    throw new BadRequestException("Para CHEQUE debe informar entidad financiera");
                }
                if (comprobanteVacio) {
                    throw new BadRequestException("Para CHEQUE debe informar numero de cheque o comprobante externo");
                }
                break;
            case CODIGO_AJUSTE:
            case CODIGO_BONIFICACION:
                if (observacionesVacio) {
                    throw new BadRequestException("Para " + codigoMetodo + " debe informar observaciones");
                }
                break;
            default:
                break;
        }
    }

    public boolean esMetodoEntregaUsado(MetodoPago metodoPago) {
        return metodoPago != null && CODIGO_ENTREGA_USADO.equals(normalizarCodigoMetodo(metodoPago));
    }

    public boolean esMetodoNoMonetarioInterno(MetodoPago metodoPago) {
        if (metodoPago == null) {
            return false;
        }
        String codigo = normalizarCodigoMetodo(metodoPago);
        return CODIGO_ENTREGA_USADO.equals(codigo) || CODIGO_PLAN_AHORRO.equals(codigo);
    }

    public void validarReglaMetodoPlanAhorro(PagoDTO pagoDTO, MetodoPago metodoPago) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        if (!CODIGO_PLAN_AHORRO.equals(codigoMetodo)) {
            return;
        }
        if (pagoDTO.getAdjudicacionPlanAhorroId() == null || pagoDTO.getContratoPlanAhorroId() == null) {
            throw new BadRequestException("El metodo PLAN_AHORRO solo puede usarse como aplicacion automatica desde una adjudicacion");
        }
    }

    public EntidadFinanciera resolverEntidadFinanciera(PagoDTO pagoDTO, MetodoPago metodoPago) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        boolean requiereEntidad = CODIGO_TRANSFERENCIA.equals(codigoMetodo) ||
            CODIGO_DEPOSITO.equals(codigoMetodo) ||
            CODIGO_CHEQUE.equals(codigoMetodo) ||
            CODIGO_TARJETA.equals(codigoMetodo) ||
            CODIGO_DEBITO.equals(codigoMetodo) ||
            CODIGO_CREDITO.equals(codigoMetodo);

        Long entidadId = pagoDTO.getEntidadFinanciera() != null ? pagoDTO.getEntidadFinanciera().getId() : null;
        if (!requiereEntidad) {
            return entidadId == null ? null : entidadFinancieraRepository.findById(entidadId).orElse(null);
        }
        if (entidadId != null) {
            return entidadFinancieraRepository
                .findById(entidadId)
                .filter(EntidadFinanciera::getActiva)
                .orElseThrow(() -> new BadRequestException("La entidad financiera no existe o no esta activa"));
        }
        if (pagoDTO.getBancoEntidad() != null && !pagoDTO.getBancoEntidad().isBlank()) {
            return null;
        }
        throw new BadRequestException("Para " + codigoMetodo + " debe informar entidad financiera");
    }

    public void completarDatosOperacion(Pago pago, MetodoPago metodoPago, TasacionUsado tasacionUsado) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        if (codigoMetodo == null) {
            codigoMetodo = "PAGO";
        }
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);

        if (CODIGO_CONTADO.equals(codigoMetodo)) {
            pago.setReferencia(null);
            pago.setNumeroOperacion(null);
            return;
        }

        if (CODIGO_ENTREGA_USADO.equals(codigoMetodo)) {
            String tasacionRef = tasacionUsado != null && tasacionUsado.getId() != null ? String.valueOf(tasacionUsado.getId()) : token;
            if (pago.getReferencia() == null || pago.getReferencia().isBlank()) {
                pago.setReferencia("TAS-" + tasacionRef);
            }
            if (pago.getNumeroOperacion() == null || pago.getNumeroOperacion().isBlank()) {
                pago.setNumeroOperacion("ENT-" + timestamp.substring(Math.max(0, timestamp.length() - 8)));
            }
            return;
        }

        if (pago.getReferencia() == null || pago.getReferencia().isBlank()) {
            pago.setReferencia(codigoMetodo + "-REF-" + token);
        }
        if (pago.getNumeroOperacion() == null || pago.getNumeroOperacion().isBlank()) {
            pago.setNumeroOperacion(codigoMetodo + "-OP-" + timestamp.substring(Math.max(0, timestamp.length() - 8)));
        }
    }

    public void completarBancoEntidadLegacy(Pago pago, EntidadFinanciera entidadFinanciera) {
        if (entidadFinanciera == null) {
            return;
        }
        if (pago.getBancoEntidad() == null || pago.getBancoEntidad().isBlank()) {
            pago.setBancoEntidad(entidadFinanciera.getNombre());
        }
    }

    public boolean esMetodoEmitibleComprobante(MetodoPago metodoPago) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        if (codigoMetodo == null) {
            return false;
        }
        return CODIGO_CONTADO.equals(codigoMetodo) ||
        CODIGO_TRANSFERENCIA.equals(codigoMetodo) ||
        CODIGO_DEPOSITO.equals(codigoMetodo) ||
        CODIGO_CHEQUE.equals(codigoMetodo) ||
        CODIGO_TARJETA.equals(codigoMetodo) ||
        CODIGO_DEBITO.equals(codigoMetodo) ||
        CODIGO_CREDITO.equals(codigoMetodo) ||
        CODIGO_ENTREGA_USADO.equals(codigoMetodo) ||
        CODIGO_SENIA.equals(codigoMetodo);
    }

    public String resolverTipoComprobantePago(MetodoPago metodoPago) {
        if (!esMetodoEmitibleComprobante(metodoPago)) {
            return null;
        }
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        return CODIGO_SENIA.equals(codigoMetodo) ? TIPO_COMPROBANTE_SEN : TIPO_COMPROBANTE_REC;
    }

    private String normalizarCodigoMetodo(MetodoPago metodoPago) {
        return pagoTextNormalizer.normalizarCodigoMetodo(metodoPago);
    }
}

