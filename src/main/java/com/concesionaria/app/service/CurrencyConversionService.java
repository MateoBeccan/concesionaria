package com.concesionaria.app.service;

import com.concesionaria.app.domain.Cotizacion;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.repository.CotizacionRepository;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.service.dto.CotizacionConversionDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CurrencyConversionService {

    private static final int FX_SCALE = 8;

    private final CotizacionRepository cotizacionRepository;
    private final MonedaRepository monedaRepository;

    @Value("${app.negocio.moneda-base-codigo:ARS}")
    private String monedaBaseCodigo;

    public CurrencyConversionService(CotizacionRepository cotizacionRepository, MonedaRepository monedaRepository) {
        this.cotizacionRepository = cotizacionRepository;
        this.monedaRepository = monedaRepository;
    }

    public CotizacionConversionDTO convertir(BigDecimal monto, Long monedaOrigenId, Long monedaDestinoId, Instant fechaOperacion) {
        if (monedaOrigenId == null || monedaDestinoId == null) {
            throw new BadRequestException("Las monedas de origen y destino son obligatorias");
        }

        Instant fechaRef = fechaOperacion != null ? fechaOperacion : Instant.now();
        BigDecimal montoRef = (monto == null ? BigDecimal.ONE : monto).setScale(2, RoundingMode.HALF_UP);
        if (montoRef.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El monto a convertir no puede ser negativo");
        }
        Moneda monedaOrigen = monedaRepository.findById(monedaOrigenId).orElseThrow(() ->
            new BadRequestException("La moneda de origen no existe")
        );
        Moneda monedaDestino = monedaRepository.findById(monedaDestinoId).orElseThrow(() ->
            new BadRequestException("La moneda de destino no existe")
        );

        CotizacionConversionDTO dto = new CotizacionConversionDTO();
        dto.setMonedaOrigenId(monedaOrigen.getId());
        dto.setMonedaOrigenCodigo(monedaOrigen.getCodigo());
        dto.setMonedaDestinoId(monedaDestino.getId());
        dto.setMonedaDestinoCodigo(monedaDestino.getCodigo());
        dto.setFechaOperacion(fechaRef);
        dto.setMontoOriginal(montoRef);

        if (monedaOrigenId.equals(monedaDestinoId)) {
            dto.setCotizacionAplicada(BigDecimal.ONE.setScale(FX_SCALE, RoundingMode.HALF_UP));
            dto.setMontoConvertido(montoRef.setScale(2, RoundingMode.HALF_UP));
            dto.setFechaCotizacionUsada(fechaRef);
            dto.setDetalleReglaAplicada("MISMA_MONEDA");
            return dto;
        }

        RateAndDate origenRate = resolverValorVentaEnMonedaBase(monedaOrigen, fechaRef);
        RateAndDate destinoRate = resolverValorVentaEnMonedaBase(monedaDestino, fechaRef);
        BigDecimal cotizacionAplicada = origenRate.rate().divide(destinoRate.rate(), FX_SCALE, RoundingMode.HALF_UP);
        if (cotizacionAplicada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("No se pudo determinar una cotizacion valida para la conversion solicitada");
        }
        BigDecimal montoConvertido = montoRef.multiply(cotizacionAplicada).setScale(2, RoundingMode.HALF_UP);

        dto.setCotizacionAplicada(cotizacionAplicada);
        dto.setMontoConvertido(montoConvertido);
        dto.setCotizacionOrigenId(origenRate.cotizacionId());
        dto.setCotizacionDestinoId(destinoRate.cotizacionId());
        dto.setFechaCotizacionUsada(resolverFechaCotizacionUsada(origenRate, destinoRate, fechaRef));
        dto.setDetalleReglaAplicada(resolverDetalleRegla(monedaOrigen, monedaDestino));
        return dto;
    }

    public CotizacionConversionDTO obtenerCotizacion(Long monedaOrigenId, Long monedaDestinoId, Instant fechaOperacion) {
        return convertir(BigDecimal.ONE, monedaOrigenId, monedaDestinoId, fechaOperacion);
    }

    private RateAndDate resolverValorVentaEnMonedaBase(Moneda moneda, Instant fechaRef) {
        if (esMonedaBase(moneda)) {
            return new RateAndDate(BigDecimal.ONE.setScale(FX_SCALE, RoundingMode.HALF_UP), null, fechaRef);
        }

        Cotizacion cotizacion = cotizacionRepository
            .findTopByMonedaIdAndActivoTrueAndFechaLessThanEqualOrderByFechaDesc(moneda.getId(), fechaRef)
            .or(() -> cotizacionRepository.findTopByMonedaIdAndFechaLessThanEqualOrderByFechaDesc(moneda.getId(), fechaRef))
            .or(() -> cotizacionRepository.findTopByMonedaIdAndActivoTrueOrderByFechaDesc(moneda.getId()))
            .or(() -> cotizacionRepository.findTopByMonedaIdOrderByFechaDesc(moneda.getId()))
            .orElseThrow(() ->
                new BadRequestException(
                    "No hay cotizacion disponible para " + moneda.getCodigo() + " en la fecha solicitada (" + fechaRef + ")"
                )
            );

        if (cotizacion.getValorVenta() == null || cotizacion.getValorVenta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La cotizacion encontrada para " + moneda.getCodigo() + " no tiene valor de venta valido");
        }

        return new RateAndDate(cotizacion.getValorVenta().setScale(FX_SCALE, RoundingMode.HALF_UP), cotizacion.getId(), cotizacion.getFecha());
    }

    private boolean esMonedaBase(Moneda moneda) {
        if (moneda == null) {
            return false;
        }
        if (moneda.getCodigo() != null && moneda.getCodigo().equalsIgnoreCase(monedaBaseCodigo)) {
            return true;
        }

        // Fallback defensivo: algunas bases historicas guardaron "PESOS ARGS" como codigo.
        String codigoNormalizado = normalizarMoneda(moneda.getCodigo());
        String descripcionNormalizada = normalizarMoneda(moneda.getDescripcion());

        return (
            codigoNormalizado.contains("ARS") ||
            codigoNormalizado.contains("ARG") ||
            codigoNormalizado.contains("PESO") ||
            descripcionNormalizada.contains("ARS") ||
            descripcionNormalizada.contains("ARG") ||
            descripcionNormalizada.contains("PESO")
        );
    }

    private String normalizarMoneda(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
    }

    private Instant resolverFechaCotizacionUsada(RateAndDate origenRate, RateAndDate destinoRate, Instant fechaRef) {
        if (origenRate.cotizacionId() == null && destinoRate.cotizacionId() == null) {
            return fechaRef;
        }
        if (origenRate.cotizacionId() == null) {
            return destinoRate.fecha();
        }
        if (destinoRate.cotizacionId() == null) {
            return origenRate.fecha();
        }
        return origenRate.fecha().isAfter(destinoRate.fecha()) ? origenRate.fecha() : destinoRate.fecha();
    }

    private String resolverDetalleRegla(Moneda origen, Moneda destino) {
        boolean origenBase = esMonedaBase(origen);
        boolean destinoBase = esMonedaBase(destino);
        if (origenBase && destinoBase) {
            return "ARS_A_ARS";
        }
        if (!origenBase && destinoBase) {
            return "MONEDA_EXTRANJERA_A_BASE";
        }
        if (origenBase) {
            return "BASE_A_MONEDA_EXTRANJERA";
        }
        return "MONEDA_EXTRANJERA_A_MONEDA_EXTRANJERA_VIA_BASE";
    }

    private record RateAndDate(BigDecimal rate, Long cotizacionId, Instant fecha) {}
}
