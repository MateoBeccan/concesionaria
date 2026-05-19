package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.repository.ComprobantePlanAhorroRepository;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.PlanAhorroRepository;
import com.concesionaria.app.repository.UserRepository;
import com.concesionaria.app.service.ComprobantePlanAhorroService;
import com.concesionaria.app.service.MovimientoCajaService;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.ContratoPlanAhorroDTO;
import com.concesionaria.app.service.dto.CuotaPlanAhorroDTO;
import com.concesionaria.app.service.dto.PlanAhorroDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ContratoPlanAhorroMapper;
import com.concesionaria.app.service.mapper.CuotaPlanAhorroMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ContratoPlanAhorroServiceImplBusinessTest {

    @Mock
    private ContratoPlanAhorroRepository contratoRepository;

    @Mock
    private CuotaPlanAhorroRepository cuotaRepository;

    @Mock
    private PlanAhorroRepository planRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private MovimientoCajaService movimientoCajaService;

    @Mock
    private ComprobantePlanAhorroService comprobantePlanAhorroService;

    @Mock
    private ComprobantePlanAhorroRepository comprobantePlanAhorroRepository;

    @Mock
    private ContratoPlanAhorroMapper contratoMapper;

    @Mock
    private CuotaPlanAhorroMapper cuotaMapper;

    private ContratoPlanAhorroServiceImpl service;

    @BeforeEach
    void setUp() {
        service =
            new ContratoPlanAhorroServiceImpl(
                contratoRepository,
                cuotaRepository,
                planRepository,
                clienteRepository,
                userRepository,
                metodoPagoRepository,
                pagoRepository,
                movimientoCajaService,
                comprobantePlanAhorroService,
                comprobantePlanAhorroRepository,
                contratoMapper,
                cuotaMapper
            );
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", "n/a", "ROLE_USER"));
    }

    @Test
    void crearContratoGeneraCuotasProrrateadasYSaldoInicialCorrecto() {
        PlanAhorro plan = planBase();
        Cliente cliente = clienteBase();
        ContratoPlanAhorroDTO input = contratoInput();

        when(planRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(clienteRepository.findById(20L)).thenReturn(Optional.of(cliente));
        when(userRepository.findOneByLogin("user")).thenReturn(Optional.of(new User()));
        when(contratoRepository.count()).thenReturn(0L);
        when(contratoRepository.existsByNumeroContrato("PLAN-000001")).thenReturn(false);
        when(contratoRepository.save(any(ContratoPlanAhorro.class))).thenAnswer(invocation -> {
            ContratoPlanAhorro c = invocation.getArgument(0);
            c.setId(99L);
            return c;
        });
        when(cuotaRepository.save(any(CuotaPlanAhorro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contratoMapper.toDto(any(ContratoPlanAhorro.class))).thenReturn(new ContratoPlanAhorroDTO());

        service.crearContrato(input);

        ArgumentCaptor<ContratoPlanAhorro> contratoCaptor = ArgumentCaptor.forClass(ContratoPlanAhorro.class);
        verify(contratoRepository).save(contratoCaptor.capture());
        assertThat(contratoCaptor.getValue().getSaldoPendiente()).isEqualByComparingTo("24500000.00");
        assertThat(contratoCaptor.getValue().getCuotasTotales()).isEqualTo(84);
        assertThat(contratoCaptor.getValue().getCuotasPagadas()).isZero();

        ArgumentCaptor<CuotaPlanAhorro> cuotaCaptor = ArgumentCaptor.forClass(CuotaPlanAhorro.class);
        verify(cuotaRepository, org.mockito.Mockito.times(84)).save(cuotaCaptor.capture());
        List<CuotaPlanAhorro> cuotas = cuotaCaptor.getAllValues();
        assertThat(cuotas.getFirst().getImporte()).isEqualByComparingTo("291666.67");
        assertThat(cuotas.getLast().getImporte()).isEqualByComparingTo("291666.39");
    }

    @Test
    void pagarCuotaRestaImporteCorrectoEnSaldoPendiente() {
        ContratoPlanAhorro contrato = new ContratoPlanAhorro();
        contrato.setId(99L);
        contrato.setEstado(EstadoContratoPlanAhorro.ACTIVO);
        contrato.setSaldoPendiente(new BigDecimal("24500000.00"));
        contrato.setPlan(planBase());

        CuotaPlanAhorro cuota = new CuotaPlanAhorro();
        cuota.setId(501L);
        cuota.setContrato(contrato);
        cuota.setEstado(EstadoCuotaPlanAhorro.PENDIENTE);
        cuota.setImporte(new BigDecimal("291666.67"));

        MetodoPago contado = new MetodoPago();
        contado.setId(1L);
        contado.setCodigo("CONTADO");

        when(cuotaRepository.findOneByIdForUser(501L, "user")).thenReturn(Optional.of(cuota));
        when(metodoPagoRepository.findByCodigoIgnoreCase("CONTADO")).thenReturn(Optional.of(contado));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cuotaRepository.save(any(CuotaPlanAhorro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cuotaRepository.findAllByContratoIdOrderByNumeroCuotaAsc(99L))
            .thenReturn(
                List.of(
                    cuotaEstado("291666.67", EstadoCuotaPlanAhorro.PAGADA),
                    cuotaEstado("24208333.33", EstadoCuotaPlanAhorro.PENDIENTE)
                )
            );
        when(contratoRepository.save(any(ContratoPlanAhorro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cuotaMapper.toDto(any(CuotaPlanAhorro.class))).thenReturn(new CuotaPlanAhorroDTO());

        service.pagarCuota(501L, new BigDecimal("291666.67"), "ok");

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        assertThat(pagoCaptor.getValue().getContratoPlanAhorro()).isNotNull();
        assertThat(pagoCaptor.getValue().getContratoPlanAhorro().getId()).isEqualTo(99L);
        assertThat(pagoCaptor.getValue().getVenta()).isNull();
        assertThat(pagoCaptor.getValue().getReserva()).isNull();
        assertThat(pagoCaptor.getValue().getTasacionUsado()).isNull();

        ArgumentCaptor<ContratoPlanAhorro> contratoCaptor = ArgumentCaptor.forClass(ContratoPlanAhorro.class);
        verify(contratoRepository).save(contratoCaptor.capture());
        assertThat(contratoCaptor.getValue().getCuotasPagadas()).isEqualTo(1);
        assertThat(contratoCaptor.getValue().getSaldoPendiente()).isEqualByComparingTo("24208333.33");
        assertThat(contratoCaptor.getValue().getEstado()).isEqualTo(EstadoContratoPlanAhorro.ACTIVO);
    }

    @Test
    void pagarCuotasMultiplesCreaUnSoloPagoYVinculaTodasLasCuotas() {
        ContratoPlanAhorro contrato = contratoBase(99L);
        CuotaPlanAhorro cuota1 = cuotaConContrato(501L, 1, "291666.67", EstadoCuotaPlanAhorro.PENDIENTE, contrato);
        CuotaPlanAhorro cuota2 = cuotaConContrato(502L, 2, "291666.67", EstadoCuotaPlanAhorro.VENCIDA, contrato);

        MetodoPago contado = new MetodoPago();
        contado.setId(1L);
        contado.setCodigo("CONTADO");

        when(cuotaRepository.findOneByIdForUser(501L, "user")).thenReturn(Optional.of(cuota1));
        when(cuotaRepository.findOneByIdForUser(502L, "user")).thenReturn(Optional.of(cuota2));
        when(comprobantePlanAhorroRepository.existsByCuotaPlanAhorroIdAndEstado(any(Long.class), eq(EstadoComprobante.EMITIDO))).thenReturn(false);
        when(metodoPagoRepository.findByCodigoIgnoreCase("CONTADO")).thenReturn(Optional.of(contado));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago pago = invocation.getArgument(0);
            pago.setId(900L);
            return pago;
        });
        when(cuotaRepository.save(any(CuotaPlanAhorro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cuotaMapper.toDto(any(CuotaPlanAhorro.class))).thenAnswer(invocation -> {
            CuotaPlanAhorro cuota = invocation.getArgument(0);
            CuotaPlanAhorroDTO dto = new CuotaPlanAhorroDTO();
            dto.setId(cuota.getId());
            dto.setPagoId(cuota.getPago() != null ? cuota.getPago().getId() : null);
            return dto;
        });
        when(cuotaRepository.findAllByContratoIdOrderByNumeroCuotaAsc(99L))
            .thenReturn(List.of(cuotaConContrato(501L, 1, "291666.67", EstadoCuotaPlanAhorro.PAGADA, contrato), cuotaConContrato(502L, 2, "291666.67", EstadoCuotaPlanAhorro.PAGADA, contrato)));
        when(contratoRepository.save(any(ContratoPlanAhorro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<CuotaPlanAhorroDTO> result = service.pagarCuotas(List.of(502L, 501L), new BigDecimal("583333.34"), "pago multiple", null, 1L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(dto -> dto.getPagoId().equals(900L));
        verify(pagoRepository).save(any(Pago.class));
        verify(cuotaRepository, org.mockito.Mockito.times(2)).save(any(CuotaPlanAhorro.class));
        verify(comprobantePlanAhorroService, org.mockito.Mockito.times(2)).emitirParaCuota(any(CuotaPlanAhorro.class), any(Pago.class));
        verify(movimientoCajaService).registrarDesdePago(any(Pago.class), eq(TipoMovimientoCaja.INGRESO), eq(EstadoPago.REGISTRADO), eq(true));
    }

    @Test
    void pagarCuotasMultiplesBloqueaCuotasDeDistintoContrato() {
        ContratoPlanAhorro contrato1 = contratoBase(99L);
        ContratoPlanAhorro contrato2 = contratoBase(100L);
        CuotaPlanAhorro cuota1 = cuotaConContrato(601L, 1, "1000.00", EstadoCuotaPlanAhorro.PENDIENTE, contrato1);
        CuotaPlanAhorro cuota2 = cuotaConContrato(602L, 1, "1000.00", EstadoCuotaPlanAhorro.PENDIENTE, contrato2);

        when(cuotaRepository.findOneByIdForUser(601L, "user")).thenReturn(Optional.of(cuota1));
        when(cuotaRepository.findOneByIdForUser(602L, "user")).thenReturn(Optional.of(cuota2));

        assertThrows(BadRequestException.class, () -> service.pagarCuotas(List.of(601L, 602L), null, null, null, null));
    }

    @Test
    void pagarCuotasMultiplesBloqueaCuotaPagada() {
        ContratoPlanAhorro contrato = contratoBase(99L);
        CuotaPlanAhorro cuota = cuotaConContrato(701L, 1, "1000.00", EstadoCuotaPlanAhorro.PAGADA, contrato);

        when(cuotaRepository.findOneByIdForUser(701L, "user")).thenReturn(Optional.of(cuota));

        assertThrows(BadRequestException.class, () -> service.pagarCuotas(List.of(701L), null, null, null, null));
    }

    private ContratoPlanAhorroDTO contratoInput() {
        ContratoPlanAhorroDTO dto = new ContratoPlanAhorroDTO();
        ClienteDTO cliente = new ClienteDTO();
        cliente.setId(20L);
        PlanAhorroDTO plan = new PlanAhorroDTO();
        plan.setId(10L);
        dto.setCliente(cliente);
        dto.setPlan(plan);
        dto.setFechaInicio(Instant.parse("2026-05-14T10:00:00Z"));
        return dto;
    }

    private PlanAhorro planBase() {
        Moneda moneda = new Moneda();
        moneda.setId(1L);
        moneda.setCodigo("ARS");

        PlanAhorro plan = new PlanAhorro();
        plan.setId(10L);
        plan.setCantidadCuotas(84);
        plan.setValorMovil(new BigDecimal("24500000.00"));
        plan.setMoneda(moneda);
        return plan;
    }

    private Cliente clienteBase() {
        Cliente cliente = new Cliente();
        cliente.setId(20L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        return cliente;
    }

    private CuotaPlanAhorro cuotaEstado(String importe, EstadoCuotaPlanAhorro estado) {
        CuotaPlanAhorro cuota = new CuotaPlanAhorro();
        cuota.setImporte(new BigDecimal(importe));
        cuota.setEstado(estado);
        return cuota;
    }

    private ContratoPlanAhorro contratoBase(Long id) {
        ContratoPlanAhorro contrato = new ContratoPlanAhorro();
        contrato.setId(id);
        contrato.setEstado(EstadoContratoPlanAhorro.ACTIVO);
        contrato.setPlan(planBase());
        return contrato;
    }

    private CuotaPlanAhorro cuotaConContrato(Long id, int numero, String importe, EstadoCuotaPlanAhorro estado, ContratoPlanAhorro contrato) {
        CuotaPlanAhorro cuota = new CuotaPlanAhorro();
        cuota.setId(id);
        cuota.setNumeroCuota(numero);
        cuota.setImporte(new BigDecimal(importe));
        cuota.setEstado(estado);
        cuota.setContrato(contrato);
        return cuota;
    }
}

