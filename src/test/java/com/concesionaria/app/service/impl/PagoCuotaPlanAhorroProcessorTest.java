package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.service.ComprobantePlanAhorroService;
import com.concesionaria.app.service.MovimientoCajaService;
import com.concesionaria.app.service.dto.CuotaPlanAhorroDTO;
import com.concesionaria.app.service.mapper.CuotaPlanAhorroMapper;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagoCuotaPlanAhorroProcessorTest {

    @Mock
    private ContratoPlanAhorroRepository contratoRepository;

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private CuotaPlanAhorroRepository cuotaRepository;

    @Mock
    private MovimientoCajaService movimientoCajaService;

    @Mock
    private ComprobantePlanAhorroService comprobantePlanAhorroService;

    @Mock
    private ContratoPlanAhorroValidator validator;

    @Mock
    private ContratoPlanAhorroCalculator calculator;

    @Mock
    private CuotaPlanAhorroMapper cuotaMapper;

    private PagoCuotaPlanAhorroProcessor processor;

    @BeforeEach
    void setUp() {
        processor =
            new PagoCuotaPlanAhorroProcessor(
                contratoRepository,
                metodoPagoRepository,
                pagoRepository,
                cuotaRepository,
                movimientoCajaService,
                comprobantePlanAhorroService,
                validator,
                calculator,
                cuotaMapper
            );
    }

    @Test
    void pagarCuotaCreaPagoVinculaCuotaRegistraCajaYEmiteComprobantePlan() {
        ContratoPlanAhorro contrato = contratoBase();
        CuotaPlanAhorro cuota = new CuotaPlanAhorro();
        cuota.setId(501L);
        cuota.setContrato(contrato);
        cuota.setNumeroCuota(3);
        cuota.setImporte(new BigDecimal("291666.67"));
        cuota.setEstado(EstadoCuotaPlanAhorro.PENDIENTE);
        MetodoPago contado = new MetodoPago();
        contado.setId(1L);
        contado.setCodigo("CONTADO");
        CuotaPlanAhorroDTO dto = new CuotaPlanAhorroDTO();
        dto.setId(501L);

        when(contratoRepository.findByIdForUpdate(99L)).thenReturn(Optional.of(contrato));
        when(metodoPagoRepository.findByCodigoIgnoreCase("CONTADO")).thenReturn(Optional.of(contado));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago pago = inv.getArgument(0);
            pago.setId(9001L);
            return pago;
        });
        when(cuotaRepository.save(any(CuotaPlanAhorro.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cuotaMapper.toDto(cuota)).thenReturn(dto);

        CuotaPlanAhorroDTO result = processor.pagarCuota(cuota, new BigDecimal("291666.67"), "pago cuota 3", "asesor");

        assertThat(result.getId()).isEqualTo(501L);
        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        Pago pago = pagoCaptor.getValue();
        assertThat(pago.getId()).isEqualTo(9001L);
        assertThat(pago.getContratoPlanAhorro()).isSameAs(contrato);
        assertThat(pago.getVenta()).isNull();
        assertThat(pago.getReserva()).isNull();
        assertThat(pago.getMonto()).isEqualByComparingTo("291666.67");
        assertThat(pago.getMontoAplicadoVenta()).isEqualByComparingTo("291666.67");
        assertThat(pago.getEstado()).isEqualTo(EstadoPago.REGISTRADO);
        assertThat(pago.getTipoMovimiento()).isEqualTo(TipoMovimientoPago.PAGO_RECIBIDO);
        assertThat(pago.getMetodoPago()).isSameAs(contado);
        assertThat(pago.getMoneda().getCodigo()).isEqualTo("ARS");
        assertThat(pago.getUsuarioRegistro()).isEqualTo("asesor");
        assertThat(cuota.getPago()).isSameAs(pago);
        assertThat(cuota.getEstado()).isEqualTo(EstadoCuotaPlanAhorro.PAGADA);
        assertThat(cuota.getFechaPago()).isNotNull();
        verify(cuotaRepository).save(cuota);
        verify(calculator).recalcularContrato(contrato);
        verify(movimientoCajaService).registrarDesdePago(eq(pago), eq(TipoMovimientoCaja.INGRESO), eq(EstadoPago.REGISTRADO), eq(true));
        verify(comprobantePlanAhorroService).emitirParaCuota(cuota, pago);
    }

    private ContratoPlanAhorro contratoBase() {
        Moneda moneda = new Moneda();
        moneda.setId(1L);
        moneda.setCodigo("ARS");
        PlanAhorro plan = new PlanAhorro();
        plan.setId(10L);
        plan.setMoneda(moneda);
        ContratoPlanAhorro contrato = new ContratoPlanAhorro();
        contrato.setId(99L);
        contrato.setPlan(plan);
        return contrato;
    }
}
