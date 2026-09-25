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
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.service.ComprobantePlanAhorroService;
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
    private PagoRegistrationService pagoRegistrationService;

    @Mock
    private CuotaPlanAhorroRepository cuotaRepository;

    @Mock
    private PagoCajaBridge pagoCajaBridge;

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
                pagoRegistrationService,
                cuotaRepository,
                pagoCajaBridge,
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
        when(pagoRegistrationService.registrar(any(RegistrarPagoCommand.class))).thenAnswer(inv -> pagoDesdeCommand(inv.getArgument(0)));
        when(cuotaRepository.save(any(CuotaPlanAhorro.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cuotaMapper.toDto(cuota)).thenReturn(dto);

        CuotaPlanAhorroDTO result = processor.pagarCuota(cuota, new BigDecimal("291666.67"), "pago cuota 3", "asesor");

        assertThat(result.getId()).isEqualTo(501L);
        ArgumentCaptor<RegistrarPagoCommand> commandCaptor = ArgumentCaptor.forClass(RegistrarPagoCommand.class);
        verify(pagoRegistrationService).registrar(commandCaptor.capture());
        RegistrarPagoCommand command = commandCaptor.getValue();
        Pago pago = cuota.getPago();
        assertThat(pago.getId()).isEqualTo(9001L);
        assertThat(command.contratoPlanAhorro()).isSameAs(contrato);
        assertThat(command.venta()).isNull();
        assertThat(command.reserva()).isNull();
        assertThat(command.monto()).isEqualByComparingTo("291666.67");
        assertThat(command.montoAplicadoVenta()).isEqualByComparingTo("291666.67");
        assertThat(command.estado()).isEqualTo(EstadoPago.REGISTRADO);
        assertThat(command.tipoMovimiento()).isEqualTo(TipoMovimientoPago.PAGO_RECIBIDO);
        assertThat(command.metodoPago()).isSameAs(contado);
        assertThat(command.moneda().getCodigo()).isEqualTo("ARS");
        assertThat(command.usuarioRegistro()).isEqualTo("asesor");
        assertThat(cuota.getPago()).isSameAs(pago);
        assertThat(cuota.getEstado()).isEqualTo(EstadoCuotaPlanAhorro.PAGADA);
        assertThat(cuota.getFechaPago()).isNotNull();
        verify(cuotaRepository).save(cuota);
        verify(calculator).recalcularContrato(contrato);
        verify(pagoCajaBridge).registrarPago(pago);
        verify(comprobantePlanAhorroService).emitirParaCuota(cuota, pago);
    }

    private Pago pagoDesdeCommand(RegistrarPagoCommand command) {
        Pago pago = new Pago();
        pago.setId(9001L);
        pago.setFecha(command.fecha());
        pago.setMonto(command.monto());
        pago.setMoneda(command.moneda());
        pago.setMetodoPago(command.metodoPago());
        pago.setTipoMovimiento(command.tipoMovimiento());
        pago.setEstado(command.estado());
        pago.setCotizacionUsada(command.cotizacionUsada());
        pago.setMontoAplicadoVenta(command.montoAplicadoVenta());
        pago.setFechaCotizacionUsada(command.fechaCotizacionUsada());
        pago.setContratoPlanAhorro(command.contratoPlanAhorro());
        pago.setUsuarioRegistro(command.usuarioRegistro());
        pago.setObservaciones(command.observaciones());
        pago.setCreatedDate(command.createdDate());
        pago.setLastModifiedDate(command.lastModifiedDate());
        return pago;
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
