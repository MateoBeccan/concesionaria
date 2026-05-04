package com.concesionaria.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Cotizacion;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.repository.CotizacionRepository;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.service.dto.CotizacionConversionDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private MonedaRepository monedaRepository;

    private CurrencyConversionService service;

    @BeforeEach
    void setUp() {
        service = new CurrencyConversionService(cotizacionRepository, monedaRepository);
        ReflectionTestUtils.setField(service, "monedaBaseCodigo", "ARS");
    }

    @Test
    void sameCurrencyReturnsOne() {
        Moneda ars = moneda(1L, "ARS");
        when(monedaRepository.findById(1L)).thenReturn(Optional.of(ars));

        CotizacionConversionDTO dto = service.convertir(new BigDecimal("100.00"), 1L, 1L, Instant.parse("2026-04-23T10:00:00Z"));

        assertThat(dto.getCotizacionAplicada()).isEqualByComparingTo("1.00000000");
        assertThat(dto.getMontoConvertido()).isEqualByComparingTo("100.00");
        assertThat(dto.getDetalleReglaAplicada()).isEqualTo("MISMA_MONEDA");
    }

    @Test
    void convertsUsingBaseCurrencyRates() {
        Moneda usd = moneda(2L, "USD");
        Moneda ars = moneda(1L, "ARS");
        when(monedaRepository.findById(2L)).thenReturn(Optional.of(usd));
        when(monedaRepository.findById(1L)).thenReturn(Optional.of(ars));

        Cotizacion usdRate = new Cotizacion();
        usdRate.setFecha(Instant.parse("2026-04-22T00:00:00Z"));
        usdRate.setValorVenta(new BigDecimal("1200"));

        when(cotizacionRepository.findTopByMonedaIdAndActivoTrueAndFechaLessThanEqualOrderByFechaDesc(eq(2L), any())).thenReturn(Optional.of(usdRate));

        CotizacionConversionDTO dto = service.convertir(new BigDecimal("10.00"), 2L, 1L, Instant.parse("2026-04-23T10:00:00Z"));

        assertThat(dto.getCotizacionAplicada()).isEqualByComparingTo("1200.00000000");
        assertThat(dto.getMontoConvertido()).isEqualByComparingTo("12000.00");
        assertThat(dto.getMonedaOrigenCodigo()).isEqualTo("USD");
        assertThat(dto.getMonedaDestinoCodigo()).isEqualTo("ARS");
        assertThat(dto.getDetalleReglaAplicada()).isEqualTo("MONEDA_EXTRANJERA_A_BASE");
    }

    @Test
    void failsWhenQuoteIsMissing() {
        Moneda usd = moneda(2L, "USD");
        Moneda ars = moneda(1L, "ARS");
        when(monedaRepository.findById(2L)).thenReturn(Optional.of(usd));
        when(monedaRepository.findById(1L)).thenReturn(Optional.of(ars));

        when(cotizacionRepository.findTopByMonedaIdAndActivoTrueAndFechaLessThanEqualOrderByFechaDesc(eq(2L), any())).thenReturn(Optional.empty());
        when(cotizacionRepository.findTopByMonedaIdAndFechaLessThanEqualOrderByFechaDesc(eq(2L), any())).thenReturn(Optional.empty());
        when(cotizacionRepository.findTopByMonedaIdAndActivoTrueOrderByFechaDesc(2L)).thenReturn(Optional.empty());
        when(cotizacionRepository.findTopByMonedaIdOrderByFechaDesc(2L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.convertir(new BigDecimal("10.00"), 2L, 1L, Instant.parse("2026-04-23T10:00:00Z")));
    }

    @Test
    void convertsFromBaseToForeign() {
        Moneda ars = moneda(1L, "ARS");
        Moneda usd = moneda(2L, "USD");
        when(monedaRepository.findById(1L)).thenReturn(Optional.of(ars));
        when(monedaRepository.findById(2L)).thenReturn(Optional.of(usd));

        Cotizacion usdRate = new Cotizacion();
        usdRate.setId(22L);
        usdRate.setFecha(Instant.parse("2026-04-22T00:00:00Z"));
        usdRate.setValorVenta(new BigDecimal("1200"));
        when(cotizacionRepository.findTopByMonedaIdAndActivoTrueAndFechaLessThanEqualOrderByFechaDesc(eq(2L), any())).thenReturn(Optional.of(usdRate));

        CotizacionConversionDTO dto = service.convertir(new BigDecimal("2400.00"), 1L, 2L, Instant.parse("2026-04-23T10:00:00Z"));

        assertThat(dto.getCotizacionAplicada()).isEqualByComparingTo("0.00083333");
        assertThat(dto.getMontoConvertido()).isEqualByComparingTo("2.00");
        assertThat(dto.getCotizacionDestinoId()).isEqualTo(22L);
    }

    @Test
    void convertsForeignToForeignViaBase() {
        Moneda usd = moneda(2L, "USD");
        Moneda eur = moneda(3L, "EUR");
        when(monedaRepository.findById(2L)).thenReturn(Optional.of(usd));
        when(monedaRepository.findById(3L)).thenReturn(Optional.of(eur));

        Cotizacion usdRate = new Cotizacion();
        usdRate.setId(22L);
        usdRate.setFecha(Instant.parse("2026-04-22T00:00:00Z"));
        usdRate.setValorVenta(new BigDecimal("1200"));
        Cotizacion eurRate = new Cotizacion();
        eurRate.setId(33L);
        eurRate.setFecha(Instant.parse("2026-04-22T00:00:00Z"));
        eurRate.setValorVenta(new BigDecimal("1500"));
        when(cotizacionRepository.findTopByMonedaIdAndActivoTrueAndFechaLessThanEqualOrderByFechaDesc(eq(2L), any())).thenReturn(Optional.of(usdRate));
        when(cotizacionRepository.findTopByMonedaIdAndActivoTrueAndFechaLessThanEqualOrderByFechaDesc(eq(3L), any())).thenReturn(Optional.of(eurRate));

        CotizacionConversionDTO dto = service.convertir(new BigDecimal("10.00"), 2L, 3L, Instant.parse("2026-04-23T10:00:00Z"));

        assertThat(dto.getCotizacionAplicada()).isEqualByComparingTo("0.80000000");
        assertThat(dto.getMontoConvertido()).isEqualByComparingTo("8.00");
        assertThat(dto.getDetalleReglaAplicada()).isEqualTo("MONEDA_EXTRANJERA_A_MONEDA_EXTRANJERA_VIA_BASE");
    }

    private Moneda moneda(Long id, String codigo) {
        Moneda moneda = new Moneda();
        moneda.setId(id);
        moneda.setCodigo(codigo);
        return moneda;
    }
}
