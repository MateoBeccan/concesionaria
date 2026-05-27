package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.ComprobantePlanAhorroService;
import com.concesionaria.app.service.MovimientoCajaService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.ComprobanteService;
import com.concesionaria.app.repository.TipoComprobanteRepository;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.PagoMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagoAnulacionServiceTest {

    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private PagoMapper pagoMapper;
    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private VentaService ventaService;
    @Mock
    private MovimientoCajaService movimientoCajaService;
    @Mock
    private ComprobanteService comprobanteService;
    @Mock
    private TipoComprobanteRepository tipoComprobanteRepository;
    @Mock
    private ComprobanteRepository comprobanteRepository;
    @Mock
    private ComprobantePlanAhorroService comprobantePlanAhorroService;
    @Mock
    private CuotaPlanAhorroRepository cuotaPlanAhorroRepository;
    @Mock
    private ContratoPlanAhorroRepository contratoPlanAhorroRepository;

    private PagoAnulacionService service;

    @BeforeEach
    void setUp() {
        PagoTextNormalizer textNormalizer = new PagoTextNormalizer();
        PagoMetodoPolicy metodoPolicy = new PagoMetodoPolicy(null, textNormalizer);
        PagoCajaBridge cajaBridge = new PagoCajaBridge(movimientoCajaService, metodoPolicy);
        PagoComprobanteBridge comprobanteBridge = new PagoComprobanteBridge(
            comprobanteService,
            tipoComprobanteRepository,
            comprobanteRepository,
            metodoPolicy
        );
        service =
            new PagoAnulacionService(
                pagoRepository,
                pagoMapper,
                ventaRepository,
                reservaRepository,
                ventaService,
                comprobantePlanAhorroService,
                cuotaPlanAhorroRepository,
                contratoPlanAhorroRepository,
                textNormalizer,
                new PagoCalculator(null, textNormalizer),
                cajaBridge,
                comprobanteBridge
            );
    }

    @Test
    void anularPagoVentaRegistraReversoYRecalculaVenta() {
        Pago pago = pagoBase(11L, "CONTADO", TipoMovimientoPago.PAGO_RECIBIDO);
        Venta venta = new Venta();
        venta.setId(31L);
        pago.setVenta(venta);
        when(pagoRepository.findById(11L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(new com.concesionaria.app.service.dto.PagoDTO());
        when(comprobanteRepository.findAllByPagoIdOrderByFechaEmisionDescIdDesc(11L)).thenReturn(List.of());
        when(pagoRepository.sumMontoByVentaId(31L)).thenReturn(new BigDecimal("0.00"));

        service.anularPago(11L, "Reversion");

        assertThat(pago.getEstado()).isEqualTo(EstadoPago.ANULADO);
        assertThat(pago.getTipoMovimiento()).isEqualTo(TipoMovimientoPago.ANULACION);
        verify(movimientoCajaService).registrarDesdePago(any(Pago.class), org.mockito.ArgumentMatchers.eq(TipoMovimientoCaja.REVERSO), org.mockito.ArgumentMatchers.eq(EstadoPago.ANULADO), org.mockito.ArgumentMatchers.eq(true));
        verify(ventaService).confirmarVenta(31L);
    }

    @Test
    void anularPagoReservaRecalculaSenia() {
        Pago pago = pagoBase(12L, "CONTADO", TipoMovimientoPago.PAGO_RECIBIDO);
        Reserva reserva = new Reserva();
        reserva.setId(44L);
        pago.setReserva(reserva);
        when(pagoRepository.findById(12L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(new com.concesionaria.app.service.dto.PagoDTO());
        when(comprobanteRepository.findAllByPagoIdOrderByFechaEmisionDescIdDesc(12L)).thenReturn(List.of());
        when(reservaRepository.findById(44L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.sumMontoByReservaId(44L)).thenReturn(new BigDecimal("1000.00"));

        service.anularPago(12L, "Reversion reserva");

        verify(reservaRepository).save(reserva);
        verify(ventaService, never()).confirmarVenta(any());
    }

    @Test
    void anularPagoCuotaPlanRevierteCuota() {
        Pago pago = pagoBase(13L, "PLAN_AHORRO", TipoMovimientoPago.PAGO_RECIBIDO);
        CuotaPlanAhorro cuota = new CuotaPlanAhorro();
        cuota.setEstado(EstadoCuotaPlanAhorro.PAGADA);
        cuota.setPago(pago);
        when(pagoRepository.findById(13L)).thenReturn(Optional.of(pago));
        when(cuotaPlanAhorroRepository.findFirstByPagoId(13L)).thenReturn(Optional.of(cuota));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(new com.concesionaria.app.service.dto.PagoDTO());
        when(comprobanteRepository.findAllByPagoIdOrderByFechaEmisionDescIdDesc(13L)).thenReturn(List.of());

        service.anularPago(13L, "Anula cuota");

        assertThat(cuota.getEstado()).isEqualTo(EstadoCuotaPlanAhorro.PENDIENTE);
        assertThat(cuota.getPago()).isNull();
        verify(cuotaPlanAhorroRepository).save(cuota);
    }

    @Test
    void bloqueaPagoYaAnulado() {
        Pago pago = pagoBase(14L, "CONTADO", TipoMovimientoPago.PAGO_RECIBIDO);
        pago.setEstado(EstadoPago.ANULADO);
        when(pagoRepository.findById(14L)).thenReturn(Optional.of(pago));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.anularPago(14L, "x"));

        assertThat(ex.getMessage()).contains("El pago ya se encuentra anulado");
    }

    @Test
    void bloqueaMotivoVacio() {
        Pago pago = pagoBase(15L, "CONTADO", TipoMovimientoPago.PAGO_RECIBIDO);
        when(pagoRepository.findById(15L)).thenReturn(Optional.of(pago));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.anularPago(15L, " "));

        assertThat(ex.getMessage()).contains("Debe informar un motivo para anular el pago");
    }

    @Test
    void bloqueaEntregaUsadoConInventarioGenerado() {
        Pago pago = pagoBase(16L, "ENTREGA_USADO", TipoMovimientoPago.ENTREGA_USADO);
        TasacionUsado tasacion = new TasacionUsado();
        tasacion.setInventarioGenerado(new Inventario().id(9L));
        pago.setTasacionUsado(tasacion);
        when(pagoRepository.findById(16L)).thenReturn(Optional.of(pago));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.anularPago(16L, "Intento"));

        assertThat(ex.getMessage()).contains("No se puede anular la entrega de usado porque ya genero inventario");
    }

    @Test
    void anulaComprobantesAsociados() {
        Pago pago = pagoBase(17L, "CONTADO", TipoMovimientoPago.PAGO_RECIBIDO);
        Venta venta = new Venta().id(200L);
        pago.setVenta(venta);
        Comprobante comprobante = new Comprobante();
        comprobante.setEstado(EstadoComprobante.EMITIDO);
        when(pagoRepository.findById(17L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(new com.concesionaria.app.service.dto.PagoDTO());
        when(comprobanteRepository.findAllByPagoIdOrderByFechaEmisionDescIdDesc(17L)).thenReturn(List.of(comprobante));

        service.anularPago(17L, "Anular comp");

        assertThat(comprobante.getEstado()).isEqualTo(EstadoComprobante.ANULADO);
        verify(comprobanteRepository).save(comprobante);
        verify(comprobantePlanAhorroService).anularPorPago(org.mockito.ArgumentMatchers.eq(17L), org.mockito.ArgumentMatchers.eq("Anular comp"), any(), any());
    }

    @Test
    void pagoSinOperacionAsociadaFalla() {
        Pago pago = pagoBase(18L, "CONTADO", TipoMovimientoPago.PAGO_RECIBIDO);
        when(pagoRepository.findById(18L)).thenReturn(Optional.of(pago));
        when(cuotaPlanAhorroRepository.findFirstByPagoId(18L)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.anularPago(18L, "x"));

        assertThat(ex.getMessage()).contains("El pago no tiene una operacion asociada");
    }

    private Pago pagoBase(Long id, String metodo, TipoMovimientoPago tipoMovimientoPago) {
        Pago pago = new Pago();
        pago.setId(id);
        pago.setEstado(EstadoPago.REGISTRADO);
        pago.setTipoMovimiento(tipoMovimientoPago);
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setCodigo(metodo);
        pago.setMetodoPago(metodoPago);
        return pago;
    }
}
