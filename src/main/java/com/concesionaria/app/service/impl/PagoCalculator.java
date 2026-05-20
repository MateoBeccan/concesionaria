package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.service.CurrencyConversionService;
import com.concesionaria.app.service.dto.CotizacionConversionDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class PagoCalculator {

    private final CurrencyConversionService currencyConversionService;
    private final PagoTextNormalizer pagoTextNormalizer;

    public PagoCalculator(CurrencyConversionService currencyConversionService, PagoTextNormalizer pagoTextNormalizer) {
        this.currencyConversionService = currencyConversionService;
        this.pagoTextNormalizer = pagoTextNormalizer;
    }

    public PagoConversionResult convertirPago(BigDecimal monto, Moneda monedaPago, Moneda monedaVenta, Instant fechaPago) {
        CotizacionConversionDTO conversion = currencyConversionService.convertir(monto, monedaPago.getId(), monedaVenta.getId(), fechaPago);
        validarConversion(conversion);
        return new PagoConversionResult(
            monedaPago,
            pagoTextNormalizer.normalizarCotizacion(conversion.getCotizacionAplicada()),
            pagoTextNormalizer.normalizarMoneda(conversion.getMontoConvertido()),
            conversion.getFechaCotizacionUsada(),
            conversion.getCotizacionOrigenId()
        );
    }

    public void validarConversion(CotizacionConversionDTO conversion) {
        if (conversion.getCotizacionAplicada() == null || conversion.getCotizacionAplicada().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Cotizaci\u00c3\u00b3n inv\u00c3\u00a1lida para aplicar el pago");
        }
        if (conversion.getMontoConvertido() == null || conversion.getMontoConvertido().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Monto convertido inv\u00c3\u00a1lido");
        }
    }

    public BigDecimal calcularMontoMinimoReserva(Venta venta, BigDecimal porcentajeMinimoReserva) {
        BigDecimal importeNeto = venta.getImporteNeto() == null ? BigDecimal.ZERO : venta.getImporteNeto();
        return pagoTextNormalizer.normalizarMoneda(importeNeto.multiply(porcentajeMinimoReserva));
    }

    public BigDecimal calcularSaldoVenta(Venta venta, BigDecimal totalPagado) {
        BigDecimal total = venta.getTotal() == null ? BigDecimal.ZERO : venta.getTotal();
        return pagoTextNormalizer.normalizarMoneda(total.subtract(totalPagado));
    }

    public BigDecimal calcularTotalPagadoProyectado(BigDecimal totalPagadoActual, BigDecimal montoAplicadoVenta) {
        return pagoTextNormalizer.normalizarMoneda(totalPagadoActual.add(montoAplicadoVenta));
    }

    record PagoConversionResult(
        Moneda monedaPago,
        BigDecimal cotizacionUsada,
        BigDecimal montoAplicado,
        Instant fechaCotizacionUsada,
        Long cotizacionOrigenId
    ) {}
}
