package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.service.CurrencyConversionService;
import com.concesionaria.app.service.dto.CotizacionConversionDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class VentaCalculator {

    private final CurrencyConversionService currencyConversionService;
    private final MonedaRepository monedaRepository;
    private final PagoRepository pagoRepository;

    public VentaCalculator(CurrencyConversionService currencyConversionService, MonedaRepository monedaRepository, PagoRepository pagoRepository) {
        this.currencyConversionService = currencyConversionService;
        this.monedaRepository = monedaRepository;
        this.pagoRepository = pagoRepository;
    }

    public BigDecimal calcularMontoMinimoReserva(BigDecimal base, BigDecimal porcentajeMinimoReserva) {
        BigDecimal importeBase = base == null ? BigDecimal.ZERO : base;
        return importeBase.multiply(porcentajeMinimoReserva).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal porcentajeMinimoReservaEscalaHumana(BigDecimal porcentajeMinimoReserva) {
        return porcentajeMinimoReserva.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    public CotizacionConversionDTO resolverConversionVehiculoAVenta(VentaDTO dto, Vehiculo vehiculo, Moneda monedaBaseVenta) {
        if (monedaBaseVenta == null || monedaBaseVenta.getId() == null) {
            throw new BadRequestException("No se pudo resolver la moneda base de venta");
        }
        if (vehiculo.getMoneda() == null || vehiculo.getMoneda().getId() == null) {
            throw new BadRequestException("El vehiculo seleccionado no tiene moneda configurada");
        }

        return currencyConversionService.convertir(vehiculo.getPrecio(), vehiculo.getMoneda().getId(), monedaBaseVenta.getId(), dto.getFecha());
    }

    public void recalcularMontosDesdeVehiculo(VentaDTO dto, BigDecimal importeConvertido) {
        BigDecimal porcentaje = dto.getPorcentajeImpuesto() == null ? new BigDecimal("21.00") : dto.getPorcentajeImpuesto();
        BigDecimal impuesto = importeConvertido.multiply(porcentaje).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = importeConvertido.add(impuesto).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPagado = dto.getTotalPagado() == null ? BigDecimal.ZERO : dto.getTotalPagado().setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldo = total.subtract(totalPagado).setScale(2, RoundingMode.HALF_UP);

        dto.setPorcentajeImpuesto(porcentaje.setScale(2, RoundingMode.HALF_UP));
        dto.setImporteNeto(importeConvertido);
        dto.setImpuesto(impuesto);
        dto.setTotal(total);
        dto.setTotalPagado(totalPagado);
        dto.setSaldo(saldo);
    }

    public MonedaDTO toMonedaDto(Vehiculo vehiculo) {
        return toMonedaDto(vehiculo.getMoneda());
    }

    public MonedaDTO toMonedaDto(Moneda moneda) {
        if (moneda == null || moneda.getId() == null) {
            throw new BadRequestException("Moneda invalida");
        }
        MonedaDTO monedaDTO = new MonedaDTO();
        monedaDTO.setId(moneda.getId());
        monedaDTO.setCodigo(moneda.getCodigo());
        monedaDTO.setDescripcion(moneda.getDescripcion());
        monedaDTO.setSimbolo(moneda.getSimbolo());
        return monedaDTO;
    }

    public Moneda resolverMonedaBaseVenta(String monedaBaseCodigo) {
        Optional<Moneda> directa = monedaRepository.findByCodigoIgnoreCase(monedaBaseCodigo);
        if (directa.isPresent()) {
            return directa.get();
        }

        return monedaRepository
            .findAll()
            .stream()
            .filter(this::esAliasMonedaBase)
            .findFirst()
            .orElseThrow(() -> new BadRequestException("No existe moneda base configurada para ventas: " + monedaBaseCodigo));
    }

    public BigDecimal totalPagadoRegistrado(Long ventaId, Venta venta) {
        BigDecimal totalPagado = pagoRepository.sumMontoByVentaId(ventaId);
        if (totalPagado == null) {
            totalPagado = BigDecimal.ZERO;
        }
        if (totalPagado.compareTo(BigDecimal.ZERO) == 0 && venta.getTotalPagado() != null) {
            totalPagado = venta.getTotalPagado();
        }
        return totalPagado.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularImporteBaseReserva(Venta venta) {
        if (venta == null || venta.getId() == null) {
            return BigDecimal.ZERO;
        }
        return venta.getImporteNeto() == null ? BigDecimal.ZERO : venta.getImporteNeto();
    }

    private boolean esAliasMonedaBase(Moneda moneda) {
        if (moneda == null) {
            return false;
        }
        String codigo = normalizarTexto(moneda.getCodigo());
        String descripcion = normalizarTexto(moneda.getDescripcion());
        return (
            codigo.contains("ARS") ||
            codigo.contains("ARG") ||
            codigo.contains("PESO") ||
            descripcion.contains("ARS") ||
            descripcion.contains("ARG") ||
            descripcion.contains("PESO")
        );
    }

    private String normalizarTexto(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
    }
}

