package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.repository.TipoComprobanteRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.dto.ComprobanteDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ComprobanteMapper;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComprobanteServiceImplBusinessTest {

    @Mock
    private ComprobanteRepository comprobanteRepository;

    @Mock
    private ComprobanteMapper comprobanteMapper;

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private TipoComprobanteRepository tipoComprobanteRepository;

    private ComprobanteServiceImpl comprobanteService;

    @BeforeEach
    void setUp() {
        comprobanteService = new ComprobanteServiceImpl(
            comprobanteRepository,
            comprobanteMapper,
            ventaRepository,
            tipoComprobanteRepository
        );
    }

    @Test
    void noEmiteComprobanteParaVentaInexistente() {
        when(ventaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> comprobanteService.emitirComprobante(10L, 1L));
    }

    @Test
    void noEmiteComprobanteParaVentaNoCompletada() {
        Venta venta = ventaBase(20L, EstadoVenta.RESERVADA, "1000", "210", "1210", moneda(1L));
        when(ventaRepository.findById(20L)).thenReturn(Optional.of(venta));

        assertThrows(BadRequestException.class, () -> comprobanteService.emitirComprobante(20L, 1L));
    }

    @Test
    void emiteComprobanteValidoParaVentaCompletada() {
        Venta venta = ventaBase(30L, EstadoVenta.PAGADA, "1000", "210", "1210", moneda(1L));
        TipoComprobante tipo = tipoComprobante(4L, "FA");
        Comprobante persisted = new Comprobante();
        persisted.setId(900L);
        persisted.setEstado(EstadoComprobante.EMITIDO);
        persisted.setNumeroComprobante("FA-00000001");
        persisted.setVenta(venta);
        persisted.setTipoComprobante(tipo);
        persisted.setMoneda(venta.getMoneda());
        persisted.setImporteNeto(venta.getImporteNeto());
        persisted.setImpuesto(venta.getImpuesto());
        persisted.setTotal(venta.getTotal());

        when(ventaRepository.findById(30L)).thenReturn(Optional.of(venta));
        when(tipoComprobanteRepository.findById(4L)).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsByVentaIdAndEstado(30L, EstadoComprobante.EMITIDO)).thenReturn(false);
        when(comprobanteRepository.findMaxNumeroCorrelativoByTipoComprobanteId(4L)).thenReturn(0L);
        when(comprobanteRepository.save(any(Comprobante.class))).thenReturn(persisted);
        when(comprobanteMapper.toDto(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante comprobante = invocation.getArgument(0);
            ComprobanteDTO dto = new ComprobanteDTO();
            dto.setId(comprobante.getId());
            dto.setNumeroComprobante(comprobante.getNumeroComprobante());
            dto.setEstado(comprobante.getEstado());
            dto.setTotal(comprobante.getTotal());
            return dto;
        });

        ComprobanteDTO result = comprobanteService.emitirComprobante(30L, 4L);

        assertThat(result.getId()).isEqualTo(900L);
        assertThat(result.getNumeroComprobante()).isEqualTo("FA-00000001");
        assertThat(result.getEstado()).isEqualTo(EstadoComprobante.EMITIDO);
    }

    @Test
    void generaNumeroIncrementalPorTipoComprobante() {
        Venta venta = ventaBase(40L, EstadoVenta.PAGADA, "2000", "420", "2420", moneda(2L));
        TipoComprobante tipo = tipoComprobante(7L, "NC");
        when(ventaRepository.findById(40L)).thenReturn(Optional.of(venta));
        when(tipoComprobanteRepository.findById(7L)).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsByVentaIdAndEstado(40L, EstadoComprobante.EMITIDO)).thenReturn(false);
        when(comprobanteRepository.findMaxNumeroCorrelativoByTipoComprobanteId(7L)).thenReturn(41L);
        when(comprobanteRepository.save(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante comprobante = invocation.getArgument(0);
            comprobante.setId(901L);
            return comprobante;
        });
        when(comprobanteMapper.toDto(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante comprobante = invocation.getArgument(0);
            ComprobanteDTO dto = new ComprobanteDTO();
            dto.setId(comprobante.getId());
            dto.setNumeroComprobante(comprobante.getNumeroComprobante());
            dto.setEstado(comprobante.getEstado());
            return dto;
        });

        ComprobanteDTO result = comprobanteService.emitirComprobante(40L, 7L);
        assertThat(result.getNumeroComprobante()).isEqualTo("NC-00000042");
        assertThat(result.getEstado()).isEqualTo(EstadoComprobante.EMITIDO);
    }

    @Test
    void noPermiteDosComprobantesActivosParaMismaVenta() {
        Venta venta = ventaBase(50L, EstadoVenta.PAGADA, "1200", "252", "1452", moneda(1L));
        when(ventaRepository.findById(50L)).thenReturn(Optional.of(venta));
        when(comprobanteRepository.existsByVentaIdAndEstado(50L, EstadoComprobante.EMITIDO)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> comprobanteService.emitirComprobante(50L, 8L));
    }

    @Test
    void anulacionLogicaFuncionaCorrectamente() {
        Venta venta = ventaBase(60L, EstadoVenta.PAGADA, "1500", "315", "1815", moneda(1L));
        Comprobante comprobante = new Comprobante();
        comprobante.setId(999L);
        comprobante.setEstado(EstadoComprobante.EMITIDO);
        comprobante.setVenta(venta);

        when(comprobanteRepository.findById(999L)).thenReturn(Optional.of(comprobante));
        when(comprobanteRepository.save(any(Comprobante.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(comprobanteMapper.toDto(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante entity = invocation.getArgument(0);
            ComprobanteDTO dto = new ComprobanteDTO();
            dto.setId(entity.getId());
            dto.setEstado(entity.getEstado());
            return dto;
        });

        ComprobanteDTO result = comprobanteService.anularComprobante(999L);

        assertThat(result.getEstado()).isEqualTo(EstadoComprobante.ANULADO);
        verify(comprobanteRepository).save(comprobante);
    }

    private Venta ventaBase(Long id, EstadoVenta estado, String neto, String impuesto, String total, Moneda moneda) {
        Venta venta = new Venta();
        venta.setId(id);
        venta.setEstado(estado);
        venta.setImporteNeto(new BigDecimal(neto));
        venta.setImpuesto(new BigDecimal(impuesto));
        venta.setTotal(new BigDecimal(total));
        venta.setMoneda(moneda);
        return venta;
    }

    private Moneda moneda(Long id) {
        Moneda moneda = new Moneda();
        moneda.setId(id);
        return moneda;
    }

    private TipoComprobante tipoComprobante(Long id, String codigo) {
        TipoComprobante tipoComprobante = new TipoComprobante();
        tipoComprobante.setId(id);
        tipoComprobante.setCodigo(codigo);
        return tipoComprobante;
    }
}
