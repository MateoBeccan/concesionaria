package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.repository.PagoRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ComprobanteNumeradorService comprobanteNumeradorService;

    private ComprobanteServiceImpl comprobanteService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        lenient().when(ventaRepository.existsAccessibleByIdForUser(any(), anyString())).thenReturn(true);
        lenient().when(comprobanteRepository.existsAccessibleByIdForUser(any(), anyString())).thenReturn(true);
        lenient().when(pagoRepository.existsAccessibleByIdForUser(any(), anyString())).thenReturn(true);
        comprobanteService = new ComprobanteServiceImpl(
            comprobanteRepository,
            comprobanteMapper,
            ventaRepository,
            tipoComprobanteRepository,
            pagoRepository,
            comprobanteNumeradorService
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
        persisted.setNumeroComprobante("FA-000001");
        persisted.setVenta(venta);
        persisted.setTipoComprobante(tipo);
        persisted.setMoneda(venta.getMoneda());
        persisted.setImporteNeto(venta.getImporteNeto());
        persisted.setImpuesto(venta.getImpuesto());
        persisted.setTotal(venta.getTotal());

        when(ventaRepository.findById(30L)).thenReturn(Optional.of(venta));
        when(tipoComprobanteRepository.findById(4L)).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsActiveVentaComprobante(30L, 4L, EstadoComprobante.EMITIDO)).thenReturn(false);
        when(comprobanteNumeradorService.siguienteNumero(4L)).thenReturn(1L);
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
        assertThat(result.getNumeroComprobante()).isEqualTo("FA-000001");
        assertThat(result.getEstado()).isEqualTo(EstadoComprobante.EMITIDO);
        verify(comprobanteRepository).existsActiveVentaComprobante(30L, 4L, EstadoComprobante.EMITIDO);
    }

    @Test
    void generaNumeroIncrementalPorTipoComprobante() {
        Venta venta = ventaBase(40L, EstadoVenta.PAGADA, "2000", "420", "2420", moneda(2L));
        TipoComprobante tipo = tipoComprobante(7L, "NC");
        when(ventaRepository.findById(40L)).thenReturn(Optional.of(venta));
        when(tipoComprobanteRepository.findById(7L)).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsActiveVentaComprobante(40L, 7L, EstadoComprobante.EMITIDO)).thenReturn(false);
        when(comprobanteNumeradorService.siguienteNumero(7L)).thenReturn(42L);
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
        assertThat(result.getNumeroComprobante()).isEqualTo("NC-000042");
        assertThat(result.getEstado()).isEqualTo(EstadoComprobante.EMITIDO);
    }

    @Test
    void noPermiteDosComprobantesActivosParaMismaVenta() {
        Venta venta = ventaBase(50L, EstadoVenta.PAGADA, "1200", "252", "1452", moneda(1L));
        when(ventaRepository.findById(50L)).thenReturn(Optional.of(venta));
        when(comprobanteRepository.existsActiveVentaComprobante(50L, 8L, EstadoComprobante.EMITIDO)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> comprobanteService.emitirComprobante(50L, 8L));
        verify(comprobanteNumeradorService, never()).siguienteNumero(8L);
    }

    @Test
    void comprobanteVentaIgnoraComprobantePagoActivoDelMismoTipo() {
        Venta venta = ventaBase(80L, EstadoVenta.PAGADA, "1000", "210", "1210", moneda(1L));
        TipoComprobante tipo = tipoComprobante(11L, "REC");

        when(ventaRepository.findById(80L)).thenReturn(Optional.of(venta));
        when(tipoComprobanteRepository.findById(11L)).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsActiveVentaComprobante(80L, 11L, EstadoComprobante.EMITIDO)).thenReturn(false);
        when(comprobanteNumeradorService.siguienteNumero(11L)).thenReturn(30L);
        when(comprobanteRepository.save(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante comprobante = invocation.getArgument(0);
            comprobante.setId(903L);
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

        ComprobanteDTO result = comprobanteService.emitirComprobante(80L, 11L);

        assertThat(result.getNumeroComprobante()).isEqualTo("REC-000030");
        verify(comprobanteRepository).existsActiveVentaComprobante(80L, 11L, EstadoComprobante.EMITIDO);
    }

    @Test
    void reemiteComprobanteVentaDespuesDeAnulacionConNuevoNumero() {
        Venta venta = ventaBase(81L, EstadoVenta.PAGADA, "1000", "210", "1210", moneda(1L));
        TipoComprobante tipo = tipoComprobante(12L, "FA");

        when(ventaRepository.findById(81L)).thenReturn(Optional.of(venta));
        when(tipoComprobanteRepository.findById(12L)).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsActiveVentaComprobante(81L, 12L, EstadoComprobante.EMITIDO)).thenReturn(false);
        when(comprobanteNumeradorService.siguienteNumero(12L)).thenReturn(45L);
        when(comprobanteRepository.save(any(Comprobante.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(comprobanteMapper.toDto(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante comprobante = invocation.getArgument(0);
            ComprobanteDTO dto = new ComprobanteDTO();
            dto.setNumeroComprobante(comprobante.getNumeroComprobante());
            dto.setEstado(comprobante.getEstado());
            return dto;
        });

        ComprobanteDTO result = comprobanteService.emitirComprobante(81L, 12L);

        assertThat(result.getNumeroComprobante()).isEqualTo("FA-000045");
        assertThat(result.getEstado()).isEqualTo(EstadoComprobante.EMITIDO);
    }

    @Test
    void emiteComprobantePagoValidoUsandoNumerador() {
        Venta venta = ventaBase(70L, EstadoVenta.PAGADA, "1000", "210", "1210", moneda(1L));
        Pago pago = new Pago();
        pago.setId(700L);
        pago.setVenta(venta);
        pago.setMonto(new BigDecimal("500.00"));
        pago.setMontoAplicadoVenta(new BigDecimal("500.00"));
        TipoComprobante tipo = tipoComprobante(9L, "REC");

        when(pagoRepository.findById(700L)).thenReturn(Optional.of(pago));
        when(ventaRepository.findById(70L)).thenReturn(Optional.of(venta));
        when(tipoComprobanteRepository.findById(9L)).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsByPagoIdAndTipoComprobanteIdAndEstado(700L, 9L, EstadoComprobante.EMITIDO)).thenReturn(false);
        when(comprobanteNumeradorService.siguienteNumero(9L)).thenReturn(12L);
        when(comprobanteRepository.save(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante comprobante = invocation.getArgument(0);
            comprobante.setId(902L);
            return comprobante;
        });
        when(comprobanteMapper.toDto(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante comprobante = invocation.getArgument(0);
            ComprobanteDTO dto = new ComprobanteDTO();
            dto.setId(comprobante.getId());
            dto.setNumeroComprobante(comprobante.getNumeroComprobante());
            dto.setEstado(comprobante.getEstado());
            dto.setTotal(comprobante.getTotal());
            return dto;
        });

        ComprobanteDTO result = comprobanteService.emitirComprobantePago(700L, 9L);

        assertThat(result.getId()).isEqualTo(902L);
        assertThat(result.getNumeroComprobante()).isEqualTo("REC-000012");
        assertThat(result.getEstado()).isEqualTo(EstadoComprobante.EMITIDO);
        ArgumentCaptor<Comprobante> comprobanteCaptor = ArgumentCaptor.forClass(Comprobante.class);
        verify(comprobanteRepository).save(comprobanteCaptor.capture());
        Comprobante comprobante = comprobanteCaptor.getValue();
        assertThat(comprobante.getNumeroComprobante()).isEqualTo("REC-000012");
        assertThat(comprobante.getPago()).isSameAs(pago);
        assertThat(comprobante.getVenta()).isSameAs(venta);
        assertThat(comprobante.getEstado()).isEqualTo(EstadoComprobante.EMITIDO);
    }

    @Test
    void noPermiteDosComprobantesActivosParaMismoPago() {
        Venta venta = ventaBase(71L, EstadoVenta.PAGADA, "1000", "210", "1210", moneda(1L));
        Pago pago = new Pago();
        pago.setId(701L);
        pago.setVenta(venta);
        TipoComprobante tipo = tipoComprobante(10L, "REC");

        when(pagoRepository.findById(701L)).thenReturn(Optional.of(pago));
        when(ventaRepository.findById(71L)).thenReturn(Optional.of(venta));
        when(tipoComprobanteRepository.findById(10L)).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsByPagoIdAndTipoComprobanteIdAndEstado(701L, 10L, EstadoComprobante.EMITIDO)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> comprobanteService.emitirComprobantePago(701L, 10L));
        verify(comprobanteNumeradorService, never()).siguienteNumero(10L);
    }

    @Test
    void permiteComprobantesPagoMismoTipoParaPagosDistintosDeLaMismaVenta() {
        Venta venta = ventaBase(90L, EstadoVenta.PAGADA, "1000", "210", "1210", moneda(1L));
        Pago pagoUno = pagoBase(901L, venta, "400.00");
        Pago pagoDos = pagoBase(902L, venta, "600.00");
        TipoComprobante tipo = tipoComprobante(13L, "REC");

        when(pagoRepository.findById(901L)).thenReturn(Optional.of(pagoUno));
        when(pagoRepository.findById(902L)).thenReturn(Optional.of(pagoDos));
        when(ventaRepository.findById(90L)).thenReturn(Optional.of(venta));
        when(tipoComprobanteRepository.findById(13L)).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsByPagoIdAndTipoComprobanteIdAndEstado(901L, 13L, EstadoComprobante.EMITIDO)).thenReturn(false);
        when(comprobanteRepository.existsByPagoIdAndTipoComprobanteIdAndEstado(902L, 13L, EstadoComprobante.EMITIDO)).thenReturn(false);
        when(comprobanteNumeradorService.siguienteNumero(13L)).thenReturn(51L, 52L);
        when(comprobanteRepository.save(any(Comprobante.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(comprobanteMapper.toDto(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante comprobante = invocation.getArgument(0);
            ComprobanteDTO dto = new ComprobanteDTO();
            dto.setNumeroComprobante(comprobante.getNumeroComprobante());
            dto.setEstado(comprobante.getEstado());
            return dto;
        });

        ComprobanteDTO primero = comprobanteService.emitirComprobantePago(901L, 13L);
        ComprobanteDTO segundo = comprobanteService.emitirComprobantePago(902L, 13L);

        assertThat(primero.getNumeroComprobante()).isEqualTo("REC-000051");
        assertThat(segundo.getNumeroComprobante()).isEqualTo("REC-000052");
    }

    @Test
    void reemiteComprobantePagoDespuesDeAnulacionConNuevoNumero() {
        Venta venta = ventaBase(91L, EstadoVenta.PAGADA, "1000", "210", "1210", moneda(1L));
        Pago pago = pagoBase(903L, venta, "500.00");
        TipoComprobante tipo = tipoComprobante(14L, "REC");

        when(pagoRepository.findById(903L)).thenReturn(Optional.of(pago));
        when(ventaRepository.findById(91L)).thenReturn(Optional.of(venta));
        when(tipoComprobanteRepository.findById(14L)).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsByPagoIdAndTipoComprobanteIdAndEstado(903L, 14L, EstadoComprobante.EMITIDO)).thenReturn(false);
        when(comprobanteNumeradorService.siguienteNumero(14L)).thenReturn(61L);
        when(comprobanteRepository.save(any(Comprobante.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(comprobanteMapper.toDto(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante comprobante = invocation.getArgument(0);
            ComprobanteDTO dto = new ComprobanteDTO();
            dto.setNumeroComprobante(comprobante.getNumeroComprobante());
            dto.setEstado(comprobante.getEstado());
            return dto;
        });

        ComprobanteDTO result = comprobanteService.emitirComprobantePago(903L, 14L);

        assertThat(result.getNumeroComprobante()).isEqualTo("REC-000061");
        assertThat(result.getEstado()).isEqualTo(EstadoComprobante.EMITIDO);
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

        ComprobanteDTO result = comprobanteService.anularComprobante(999L, "Error de emisión");

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

    private Pago pagoBase(Long id, Venta venta, String monto) {
        Pago pago = new Pago();
        pago.setId(id);
        pago.setVenta(venta);
        pago.setMonto(new BigDecimal(monto));
        pago.setMontoAplicadoVenta(new BigDecimal(monto));
        return pago;
    }
}
