package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoTasacionUsado;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.TasacionUsadoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.repository.TipoComprobanteRepository;
import com.concesionaria.app.repository.EntidadFinancieraRepository;
import com.concesionaria.app.service.CurrencyConversionService;
import com.concesionaria.app.service.ComprobanteService;
import com.concesionaria.app.service.MovimientoCajaService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.CotizacionConversionDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.PagoMapper;
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
class PagoServiceImplBusinessTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PagoMapper pagoMapper;

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private VentaService ventaService;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private MonedaRepository monedaRepository;

    @Mock
    private CurrencyConversionService currencyConversionService;

    @Mock
    private TasacionUsadoRepository tasacionUsadoRepository;

    @Mock
    private EntidadFinancieraRepository entidadFinancieraRepository;

    @Mock
    private ComprobanteService comprobanteService;

    @Mock
    private TipoComprobanteRepository tipoComprobanteRepository;

    @Mock
    private ComprobanteRepository comprobanteRepository;

    @Mock
    private MovimientoCajaService movimientoCajaService;

    private PagoServiceImpl pagoService;

    @BeforeEach
    void setUp() {
        pagoService =
            new PagoServiceImpl(
                pagoRepository,
                pagoMapper,
                ventaRepository,
                reservaRepository,
                ventaService,
                metodoPagoRepository,
                monedaRepository,
                entidadFinancieraRepository,
                tasacionUsadoRepository,
                currencyConversionService,
                comprobanteService,
                tipoComprobanteRepository,
                comprobanteRepository,
                movimientoCajaService
            );
        ReflectionTestUtils.setField(pagoService, "monedaBaseCodigo", "ARS");
    }

    @Test
    void noPermiteMontoMenorOIgualACero() {
        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.ZERO);

        assertThrows(BadRequestException.class, () -> pagoService.registrarPago(10L, pagoDTO));
    }

    @Test
    void noPermiteSobrepago() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(20L, "1000", "900", "100", EstadoVenta.PENDIENTE);
        venta.setImporteNeto(BigDecimal.valueOf(1000));
        MetodoPago efectivo = metodoPago(1L, "CONTADO", false);
        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.valueOf(150));
        pagoDTO.setMoneda(monedaDto(1L));
        pagoDTO.setMetodoPago(metodoDto(1L));
        venta.setMoneda(moneda(1L, "ARS"));

        when(ventaRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(venta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(efectivo));
        when(monedaRepository.findById(1L)).thenReturn(Optional.of(moneda(1L, "ARS")));
        when(currencyConversionService.convertir(any(), eq(1L), eq(1L), any())).thenReturn(conversion("150.00", "1"));

        assertThrows(BadRequestException.class, () -> pagoService.registrarPago(20L, pagoDTO));
    }

    @Test
    void pagoParcialValidoRecalculaVentaYSincronizaInventario() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(30L, "10000", "0", "10000", EstadoVenta.PENDIENTE);
        venta.setImporteNeto(BigDecimal.valueOf(10000));
        venta.setMoneda(moneda(1L, "ARS"));
        MetodoPago efectivo = metodoPago(1L, "CONTADO", false);

        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.valueOf(1500));
        pagoDTO.setMoneda(monedaDto(1L));
        pagoDTO.setMetodoPago(metodoDto(1L));
        Pago pago = new Pago();
        pago.setMonto(BigDecimal.valueOf(1500));

        when(ventaRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(venta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(efectivo));
        when(monedaRepository.findById(1L)).thenReturn(Optional.of(moneda(1L, "ARS")));
        when(currencyConversionService.convertir(any(), eq(1L), eq(1L), any())).thenReturn(conversion("1500.00", "1"));
        when(pagoRepository.sumMontoByVentaId(30L)).thenReturn(BigDecimal.ZERO, BigDecimal.valueOf(1500));
        when(pagoMapper.toEntity(any(PagoDTO.class))).thenReturn(pago);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDTO);

        PagoDTO result = pagoService.registrarPago(30L, pagoDTO);

        assertThat(result).isNotNull();
        assertThat(pago.getEstado()).isEqualTo(EstadoPago.REGISTRADO);
        assertThat(venta.getTotalPagado()).isEqualByComparingTo("1500.00");
        assertThat(venta.getSaldo()).isEqualByComparingTo("8500.00");
        verify(movimientoCajaService).registrarDesdePago(any(Pago.class), any(), eq(EstadoPago.REGISTRADO), eq(true));
        verify(ventaRepository).findByIdForUpdate(30L);
        verify(ventaService).sincronizarInventarioConVenta(30L);
        verify(ventaService, never()).confirmarVenta(any());
    }

    @Test
    void contadoNoExigeReferenciaNiNumeroOperacion() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(31L, "10000", "0", "10000", EstadoVenta.PENDIENTE);
        venta.setImporteNeto(BigDecimal.valueOf(10000));
        venta.setMoneda(moneda(1L, "ARS"));
        MetodoPago contado = metodoPago(1L, "CONTADO", false);

        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.valueOf(1000));
        pagoDTO.setMoneda(monedaDto(1L));
        pagoDTO.setMetodoPago(metodoDto(1L));
        pagoDTO.setReferencia("   ");
        pagoDTO.setNumeroOperacion("   ");
        Pago pago = new Pago();

        when(ventaRepository.findByIdForUpdate(31L)).thenReturn(Optional.of(venta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(contado));
        when(monedaRepository.findById(1L)).thenReturn(Optional.of(moneda(1L, "ARS")));
        when(currencyConversionService.convertir(any(), eq(1L), eq(1L), any())).thenReturn(conversion("1000.00", "1"));
        when(pagoRepository.sumMontoByVentaId(31L)).thenReturn(BigDecimal.ZERO, BigDecimal.valueOf(1000));
        when(pagoMapper.toEntity(any(PagoDTO.class))).thenReturn(pago);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDTO);

        pagoService.registrarPago(31L, pagoDTO);

        assertThat(pago.getReferencia()).isNull();
        assertThat(pago.getNumeroOperacion()).isNull();
    }

    @Test
    void pagoTotalConfirmaVenta() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(40L, "500", "0", "500", EstadoVenta.PENDIENTE);
        venta.setImporteNeto(BigDecimal.valueOf(500));
        venta.setMoneda(moneda(1L, "ARS"));
        MetodoPago efectivo = metodoPago(1L, "CONTADO", false);

        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.valueOf(500));
        pagoDTO.setMoneda(monedaDto(1L));
        pagoDTO.setMetodoPago(metodoDto(1L));
        pagoDTO.setFecha(Instant.now());
        Pago pago = new Pago();
        pago.setMonto(BigDecimal.valueOf(500));

        when(ventaRepository.findByIdForUpdate(40L)).thenReturn(Optional.of(venta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(efectivo));
        when(monedaRepository.findById(1L)).thenReturn(Optional.of(moneda(1L, "ARS")));
        when(currencyConversionService.convertir(any(), eq(1L), eq(1L), any())).thenReturn(conversion("500.00", "1"));
        when(pagoRepository.sumMontoByVentaId(40L)).thenReturn(BigDecimal.ZERO, BigDecimal.valueOf(500));
        when(pagoMapper.toEntity(any(PagoDTO.class))).thenReturn(pago);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDTO);

        pagoService.registrarPago(40L, pagoDTO);

        assertThat(venta.getSaldo()).isEqualByComparingTo("0.00");
        verify(ventaService).confirmarVenta(40L);
        verify(ventaService, never()).sincronizarInventarioConVenta(eq(40L));
    }

    @Test
    void noPermitePagosSobreVentaCancelada() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(50L, "1000", "0", "1000", EstadoVenta.CANCELADA);
        venta.setMoneda(moneda(1L, "ARS"));
        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.valueOf(100));

        when(ventaRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(venta));

        assertThrows(BadRequestException.class, () -> pagoService.registrarPago(50L, pagoDTO));
    }

    @Test
    void anularPagoRecalculaVentaYSincronizaInventario() {
        Venta venta = ventaBase(60L, "1000", "200", "800", EstadoVenta.RESERVADA);
        venta.setMoneda(moneda(1L, "ARS"));
        Pago pago = new Pago();
        pago.setId(601L);
        pago.setEstado(EstadoPago.REGISTRADO);
        pago.setVenta(venta);

        when(pagoRepository.findById(601L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoRepository.sumMontoByVentaId(60L)).thenReturn(BigDecimal.ZERO);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toDto(any(Pago.class))).thenAnswer(inv -> {
            Pago entidad = inv.getArgument(0);
            PagoDTO dto = new PagoDTO();
            dto.setId(entidad.getId());
            dto.setEstado(entidad.getEstado());
            return dto;
        });

        PagoDTO resultado = pagoService.anularPago(601L, "Cliente solicitó anulación");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPago.ANULADO);
        assertThat(venta.getTotalPagado()).isEqualByComparingTo("0.00");
        assertThat(venta.getSaldo()).isEqualByComparingTo("1000.00");
        verify(movimientoCajaService).registrarDesdePago(any(Pago.class), any(), eq(EstadoPago.ANULADO), eq(true));
        verify(ventaService).sincronizarInventarioConVenta(60L);
        verify(ventaService, never()).confirmarVenta(any());
    }

    @Test
    void noPermiteAnularPagoYaAnulado() {
        Pago pago = new Pago();
        pago.setId(701L);
        pago.setEstado(EstadoPago.ANULADO);
        pago.setVenta(ventaBase(70L, "1000", "0", "1000", EstadoVenta.PENDIENTE));

        when(pagoRepository.findById(701L)).thenReturn(Optional.of(pago));

        assertThrows(BadRequestException.class, () -> pagoService.anularPago(701L, "Duplicado"));
    }

    @Test
    void noPermitePagoSiVentaNoEstaEnMonedaBase() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(80L, "1000", "0", "1000", EstadoVenta.PENDIENTE);
        venta.setMoneda(moneda(2L, "USD"));
        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.valueOf(100));
        pagoDTO.setMoneda(monedaDto(2L));

        when(ventaRepository.findByIdForUpdate(80L)).thenReturn(Optional.of(venta));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> pagoService.registrarPago(80L, pagoDTO));
        assertThat(ex.getMessage()).contains("La venta debe estar registrada en moneda base configurada: ARS");
    }

    @Test
    void noPermitePagoConCotizacionInvalida() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(90L, "1000", "0", "1000", EstadoVenta.PENDIENTE);
        venta.setImporteNeto(BigDecimal.valueOf(1000));
        venta.setMoneda(moneda(1L, "ARS"));
        MetodoPago efectivo = metodoPago(1L, "CONTADO", false);
        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.valueOf(100));
        pagoDTO.setMoneda(monedaDto(2L));
        pagoDTO.setMetodoPago(metodoDto(1L));

        CotizacionConversionDTO conversion = new CotizacionConversionDTO();
        conversion.setMontoConvertido(BigDecimal.valueOf(100));
        conversion.setCotizacionAplicada(BigDecimal.ZERO);

        when(ventaRepository.findByIdForUpdate(90L)).thenReturn(Optional.of(venta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(efectivo));
        when(monedaRepository.findById(2L)).thenReturn(Optional.of(moneda(2L, "USD")));
        when(currencyConversionService.convertir(any(), eq(2L), eq(1L), any())).thenReturn(conversion);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> pagoService.registrarPago(90L, pagoDTO));
        assertThat(ex.getMessage()).contains("Cotización inválida para aplicar el pago");
    }

    @Test
    void aplicaPagoUsdAVentaArsConMontoConvertido() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(100L, "20000", "0", "20000", EstadoVenta.PENDIENTE);
        venta.setImporteNeto(BigDecimal.valueOf(20000));
        venta.setMoneda(moneda(1L, "ARS"));
        MetodoPago efectivo = metodoPago(1L, "CONTADO", false);

        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.valueOf(10));
        pagoDTO.setMoneda(monedaDto(2L));
        pagoDTO.setMetodoPago(metodoDto(1L));
        Pago pago = new Pago();
        pago.setMonto(BigDecimal.valueOf(10));

        CotizacionConversionDTO conversion = conversion("12000.00", "1200.12345678");
        conversion.setCotizacionOrigenId(99L);

        when(ventaRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(venta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(efectivo));
        when(monedaRepository.findById(2L)).thenReturn(Optional.of(moneda(2L, "USD")));
        when(currencyConversionService.convertir(any(), eq(2L), eq(1L), any())).thenReturn(conversion);
        when(pagoRepository.sumMontoByVentaId(100L)).thenReturn(BigDecimal.ZERO, BigDecimal.valueOf(12000));
        when(pagoMapper.toEntity(any(PagoDTO.class))).thenReturn(pago);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDTO);

        pagoService.registrarPago(100L, pagoDTO);

        assertThat(pago.getMontoAplicadoVenta()).isEqualByComparingTo("12000.00");
        assertThat(pago.getCotizacionUsada()).isEqualByComparingTo("1200.12345678");
        assertThat(pago.getCotizacionRef()).isNotNull();
        assertThat(pago.getCotizacionRef().getId()).isEqualTo(99L);
    }

    @Test
    void registraEntregaUsadoComoPagoParcial() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(110L, "30000", "0", "30000", EstadoVenta.PENDIENTE);
        venta.setImporteNeto(BigDecimal.valueOf(30000));
        venta.setMoneda(moneda(1L, "ARS"));
        venta.setCliente(cliente(55L));

        MetodoPago metodo = new MetodoPago();
        metodo.setId(8L);
        metodo.setCodigo("ENTREGA_USADO");
        metodo.setRequiereReferencia(false);

        TasacionUsado tasacion = new TasacionUsado();
        tasacion.setId(900L);
        tasacion.setCliente(cliente(55L));
        tasacion.setEstado(EstadoTasacionUsado.ACEPTADA);
        tasacion.setMontoTasacion(BigDecimal.valueOf(12000));
        tasacion.setFechaTasacion(Instant.now());
        tasacion.setMoneda(moneda(1L, "ARS"));

        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.ONE);
        pagoDTO.setMetodoPago(metodoDto(8L));
        pagoDTO.setTasacionUsadoId(900L);

        Pago pago = new Pago();
        when(ventaRepository.findByIdForUpdate(110L)).thenReturn(Optional.of(venta));
        when(metodoPagoRepository.findById(8L)).thenReturn(Optional.of(metodo));
        when(tasacionUsadoRepository.findById(900L)).thenReturn(Optional.of(tasacion));
        when(tasacionUsadoRepository.existsAceptadaDisponibleByIdAndClienteId(900L, 55L)).thenReturn(true);
        when(ventaRepository.existsByTasacionUsadoIdAndIdNot(900L, 110L)).thenReturn(false);
        when(pagoRepository.sumMontoByVentaId(110L)).thenReturn(BigDecimal.ZERO, BigDecimal.valueOf(12000));
        when(pagoMapper.toEntity(any(PagoDTO.class))).thenReturn(pago);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDTO);

        pagoService.registrarPago(110L, pagoDTO);

        assertThat(pago.getTipoMovimiento()).isEqualTo(TipoMovimientoPago.ENTREGA_USADO);
        assertThat(pago.getMontoAplicadoVenta()).isEqualByComparingTo("12000.00");
        assertThat(pago.getCotizacionUsada()).isEqualByComparingTo("1.00000000");
        assertThat(pago.getTasacionUsado()).isNotNull();
        assertThat(venta.getTasacionUsado()).isNotNull();
        verify(movimientoCajaService).registrarDesdePago(any(Pago.class), any(), eq(EstadoPago.REGISTRADO), eq(false));
        verify(ventaService).sincronizarInventarioConVenta(110L);
    }

    @Test
    void bloqueaEntregaUsadoConTasacionNoAceptada() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(111L, "30000", "0", "30000", EstadoVenta.PENDIENTE);
        venta.setImporteNeto(BigDecimal.valueOf(30000));
        venta.setMoneda(moneda(1L, "ARS"));
        venta.setCliente(cliente(55L));

        MetodoPago metodo = new MetodoPago();
        metodo.setId(8L);
        metodo.setCodigo("ENTREGA_USADO");
        metodo.setRequiereReferencia(false);

        TasacionUsado tasacion = new TasacionUsado();
        tasacion.setId(901L);
        tasacion.setCliente(cliente(55L));
        tasacion.setEstado(EstadoTasacionUsado.PENDIENTE);
        tasacion.setMontoTasacion(BigDecimal.valueOf(12000));
        tasacion.setMoneda(moneda(1L, "ARS"));

        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.ONE);
        pagoDTO.setMetodoPago(metodoDto(8L));
        pagoDTO.setTasacionUsadoId(901L);

        when(ventaRepository.findByIdForUpdate(111L)).thenReturn(Optional.of(venta));
        when(metodoPagoRepository.findById(8L)).thenReturn(Optional.of(metodo));
        when(tasacionUsadoRepository.findById(901L)).thenReturn(Optional.of(tasacion));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> pagoService.registrarPago(111L, pagoDTO));
        assertThat(ex.getMessage()).contains("tasacion debe estar aceptada");
    }

    @Test
    void bloqueaEntregaUsadoConTasacionEnMonedaNoBase() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(112L, "30000", "0", "30000", EstadoVenta.PENDIENTE);
        venta.setImporteNeto(BigDecimal.valueOf(30000));
        venta.setMoneda(moneda(1L, "ARS"));
        venta.setCliente(cliente(55L));

        MetodoPago metodo = new MetodoPago();
        metodo.setId(8L);
        metodo.setCodigo("ENTREGA_USADO");
        metodo.setRequiereReferencia(false);

        TasacionUsado tasacion = new TasacionUsado();
        tasacion.setId(902L);
        tasacion.setCliente(cliente(55L));
        tasacion.setEstado(EstadoTasacionUsado.ACEPTADA);
        tasacion.setMontoTasacion(BigDecimal.valueOf(12000));
        tasacion.setMoneda(moneda(2L, "USD"));

        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.ONE);
        pagoDTO.setMetodoPago(metodoDto(8L));
        pagoDTO.setTasacionUsadoId(902L);

        when(ventaRepository.findByIdForUpdate(112L)).thenReturn(Optional.of(venta));
        when(metodoPagoRepository.findById(8L)).thenReturn(Optional.of(metodo));
        when(tasacionUsadoRepository.findById(902L)).thenReturn(Optional.of(tasacion));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> pagoService.registrarPago(112L, pagoDTO));
        assertThat(ex.getMessage()).contains("moneda base configurada: ARS");
    }

    @Test
    void anularEntregaUsadoAntesDeGenerarInventarioLiberaTasacion() {
        Venta venta = ventaBase(113L, "30000", "12000", "18000", EstadoVenta.RESERVADA);
        venta.setMoneda(moneda(1L, "ARS"));

        TasacionUsado tasacion = new TasacionUsado();
        tasacion.setId(903L);
        venta.setTasacionUsado(tasacion);

        Pago pago = new Pago();
        pago.setId(9030L);
        pago.setEstado(EstadoPago.REGISTRADO);
        pago.setTipoMovimiento(TipoMovimientoPago.ENTREGA_USADO);
        pago.setVenta(venta);
        pago.setTasacionUsado(tasacion);

        when(pagoRepository.findById(9030L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoRepository.sumMontoByVentaId(113L)).thenReturn(BigDecimal.ZERO);
        when(pagoMapper.toDto(any(Pago.class))).thenAnswer(inv -> {
            Pago entidad = inv.getArgument(0);
            PagoDTO dto = new PagoDTO();
            dto.setId(entidad.getId());
            dto.setEstado(entidad.getEstado());
            return dto;
        });

        pagoService.anularPago(9030L, "Operacion revertida");

        assertThat(venta.getTasacionUsado()).isNull();
        verify(ventaService).sincronizarInventarioConVenta(113L);
    }

    @Test
    void bloqueaAnulacionEntregaUsadoSiYaGeneroInventario() {
        Venta venta = ventaBase(114L, "30000", "12000", "18000", EstadoVenta.RESERVADA);
        venta.setMoneda(moneda(1L, "ARS"));

        TasacionUsado tasacion = new TasacionUsado();
        tasacion.setId(904L);
        Inventario inventario = new Inventario();
        inventario.setId(444L);
        tasacion.setInventarioGenerado(inventario);

        Pago pago = new Pago();
        pago.setId(9040L);
        pago.setEstado(EstadoPago.REGISTRADO);
        pago.setTipoMovimiento(TipoMovimientoPago.ENTREGA_USADO);
        pago.setVenta(venta);
        pago.setTasacionUsado(tasacion);

        when(pagoRepository.findById(9040L)).thenReturn(Optional.of(pago));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> pagoService.anularPago(9040L, "Intento anular"));
        assertThat(ex.getMessage()).contains("ya genero inventario");
    }

    @Test
    void transferenciaSinBancoEntidadFalla() {
        stubMonedaBaseArs();
        Venta venta = ventaBase(120L, "10000", "0", "10000", EstadoVenta.PENDIENTE);
        venta.setImporteNeto(BigDecimal.valueOf(10000));
        venta.setMoneda(moneda(1L, "ARS"));

        MetodoPago transferencia = new MetodoPago();
        transferencia.setId(3L);
        transferencia.setCodigo("TRANSFERENCIA");
        transferencia.setRequiereReferencia(false);

        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.valueOf(1000));
        pagoDTO.setMoneda(monedaDto(1L));
        pagoDTO.setMetodoPago(metodoDto(3L));

        when(ventaRepository.findByIdForUpdate(120L)).thenReturn(Optional.of(venta));
        when(metodoPagoRepository.findById(3L)).thenReturn(Optional.of(transferencia));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> pagoService.registrarPago(120L, pagoDTO));
        assertThat(ex.getMessage()).contains("debe informar entidad financiera");
    }

    @Test
    void reservaNoPermiteMetodoEntregaUsado() {
        Reserva reserva = new Reserva();
        reserva.setId(130L);
        reserva.setEstado(com.concesionaria.app.domain.enumeration.EstadoReserva.ACTIVA);
        reserva.setMoneda(moneda(1L, "ARS"));

        MetodoPago metodo = new MetodoPago();
        metodo.setId(8L);
        metodo.setCodigo("ENTREGA_USADO");
        metodo.setRequiereReferencia(false);

        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.valueOf(1000));
        pagoDTO.setFecha(Instant.now());
        pagoDTO.setMetodoPago(metodoDto(8L));
        pagoDTO.setMoneda(monedaDto(1L));

        when(reservaRepository.findById(130L)).thenReturn(Optional.of(reserva));
        when(metodoPagoRepository.findById(8L)).thenReturn(Optional.of(metodo));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> pagoService.registrarPagoReserva(130L, pagoDTO));
        assertThat(ex.getMessage()).contains("ENTREGA_USADO solo puede registrarse sobre una venta");
    }

    @Test
    void pagoConFechaFuturaDebeFallar() {
        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setMonto(BigDecimal.valueOf(1000));
        pagoDTO.setFecha(Instant.now().plusSeconds(3600));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> pagoService.registrarPago(10L, pagoDTO));
        assertThat(ex.getMessage()).contains("no puede estar en el futuro");
    }

    private Venta ventaBase(Long id, String total, String totalPagado, String saldo, EstadoVenta estado) {
        Venta venta = new Venta();
        venta.setId(id);
        venta.setEstado(estado);
        venta.setTotal(new BigDecimal(total));
        venta.setTotalPagado(new BigDecimal(totalPagado));
        venta.setSaldo(new BigDecimal(saldo));
        return venta;
    }

    private Moneda moneda(Long id, String codigo) {
        Moneda moneda = new Moneda();
        moneda.setId(id);
        moneda.setCodigo(codigo);
        return moneda;
    }

    private MonedaDTO monedaDto(Long id) {
        MonedaDTO moneda = new MonedaDTO();
        moneda.setId(id);
        return moneda;
    }

    private com.concesionaria.app.domain.Cliente cliente(Long id) {
        com.concesionaria.app.domain.Cliente cliente = new com.concesionaria.app.domain.Cliente();
        cliente.setId(id);
        return cliente;
    }

    private com.concesionaria.app.service.dto.MetodoPagoDTO metodoDto(Long id) {
        com.concesionaria.app.service.dto.MetodoPagoDTO metodo = new com.concesionaria.app.service.dto.MetodoPagoDTO();
        metodo.setId(id);
        return metodo;
    }

    private MetodoPago metodoPago(Long id, String codigo, boolean requiereReferencia) {
        MetodoPago metodo = new MetodoPago();
        metodo.setId(id);
        metodo.setCodigo(codigo);
        metodo.setRequiereReferencia(requiereReferencia);
        return metodo;
    }

    private CotizacionConversionDTO conversion(String montoConvertido, String cotizacion) {
        CotizacionConversionDTO dto = new CotizacionConversionDTO();
        dto.setMontoConvertido(new BigDecimal(montoConvertido));
        dto.setCotizacionAplicada(new BigDecimal(cotizacion));
        dto.setFechaCotizacionUsada(Instant.now());
        return dto;
    }

    private void stubMonedaBaseArs() {
        when(monedaRepository.findByCodigoIgnoreCase("ARS")).thenReturn(Optional.of(moneda(1L, "ARS")));
    }
}

