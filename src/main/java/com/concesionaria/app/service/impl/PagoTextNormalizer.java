package com.concesionaria.app.service.impl;

import com.concesionaria.app.config.BusinessProperties;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class PagoTextNormalizer {

    public static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
    private final BusinessProperties businessProperties;

    public PagoTextNormalizer() {
        this(BusinessProperties.defaults());
    }

    public PagoTextNormalizer(BusinessProperties businessProperties) {
        this.businessProperties = businessProperties == null ? BusinessProperties.defaults() : businessProperties;
    }

    public String normalizarTexto(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > max) {
            throw new BadRequestException("Un campo de texto excede el maximo permitido de " + max + " caracteres");
        }
        return trimmed;
    }

    public String normalizarCodigoMetodo(MetodoPago metodoPago) {
        if (metodoPago == null || metodoPago.getCodigo() == null) {
            return null;
        }
        return metodoPago.getCodigo().trim().toUpperCase(Locale.ROOT);
    }

    public BigDecimal normalizarMoneda(BigDecimal valor) {
        return valor.setScale(businessProperties.getPagos().getEscalaMonetaria(), REDONDEO);
    }

    public BigDecimal normalizarCotizacion(BigDecimal valor) {
        return valor.setScale(businessProperties.getPagos().getEscalaCotizacion(), REDONDEO);
    }
}

