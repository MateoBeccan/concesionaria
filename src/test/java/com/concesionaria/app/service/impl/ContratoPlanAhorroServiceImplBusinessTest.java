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
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.repository.ComprobantePlanAhorroRepository;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.repository.PlanAhorroRepository;
import com.concesionaria.app.repository.UserRepository;
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
    private ComprobantePlanAhorroRepository comprobantePlanAhorroRepository;

    @Mock
    private ContratoPlanAhorroValidator contratoPlanAhorroValidator;

    @Mock
    private CuotaPlanAhorroGenerator cuotaPlanAhorroGenerator;

    @Mock
    private PagoCuotaPlanAhorroProcessor pagoCuotaPlanAhorroProcessor;

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
                comprobantePlanAhorroRepository,
                contratoPlanAhorroValidator,
                cuotaPlanAhorroGenerator,
                pagoCuotaPlanAhorroProcessor,
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
        when(contratoMapper.toDto(any(ContratoPlanAhorro.class))).thenReturn(new ContratoPlanAhorroDTO());

        service.crearContrato(input);

        ArgumentCaptor<ContratoPlanAhorro> contratoCaptor = ArgumentCaptor.forClass(ContratoPlanAhorro.class);
        verify(contratoRepository).save(contratoCaptor.capture());
        assertThat(contratoCaptor.getValue().getSaldoPendiente()).isEqualByComparingTo("24500000.00");
        assertThat(contratoCaptor.getValue().getCuotasTotales()).isEqualTo(84);
        assertThat(contratoCaptor.getValue().getCuotasPagadas()).isZero();

        verify(cuotaPlanAhorroGenerator).generarCuotas(eq(contratoCaptor.getValue()), eq(plan), eq(input.getFechaInicio()));
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

        when(cuotaRepository.findOneByIdForUser(501L, "user")).thenReturn(Optional.of(cuota));
        when(pagoCuotaPlanAhorroProcessor.pagarCuota(eq(cuota), eq(new BigDecimal("291666.67")), eq("ok"), eq("user")))
            .thenReturn(new CuotaPlanAhorroDTO());

        service.pagarCuota(501L, new BigDecimal("291666.67"), "ok");

        verify(pagoCuotaPlanAhorroProcessor).pagarCuota(eq(cuota), eq(new BigDecimal("291666.67")), eq("ok"), eq("user"));
    }

    @Test
    void pagarCuotasMultiplesCreaUnSoloPagoYVinculaTodasLasCuotas() {
        ContratoPlanAhorro contrato = contratoBase(99L);
        CuotaPlanAhorro cuota1 = cuotaConContrato(501L, 1, "291666.67", EstadoCuotaPlanAhorro.PENDIENTE, contrato);
        CuotaPlanAhorro cuota2 = cuotaConContrato(502L, 2, "291666.67", EstadoCuotaPlanAhorro.VENCIDA, contrato);

        when(cuotaRepository.findOneByIdForUser(501L, "user")).thenReturn(Optional.of(cuota1));
        when(cuotaRepository.findOneByIdForUser(502L, "user")).thenReturn(Optional.of(cuota2));
        CuotaPlanAhorroDTO cuotaUnoDto = new CuotaPlanAhorroDTO();
        cuotaUnoDto.setId(501L);
        CuotaPlanAhorroDTO cuotaDosDto = new CuotaPlanAhorroDTO();
        cuotaDosDto.setId(502L);
        when(
            pagoCuotaPlanAhorroProcessor.pagarCuotas(
                any(List.class),
                eq(new BigDecimal("583333.34")),
                eq("pago multiple"),
                eq(null),
                eq(1L),
                eq("user")
            )
        )
            .thenReturn(List.of(cuotaUnoDto, cuotaDosDto));

        List<CuotaPlanAhorroDTO> result = service.pagarCuotas(List.of(502L, 501L), new BigDecimal("583333.34"), "pago multiple", null, 1L);

        assertThat(result).hasSize(2);
        verify(pagoCuotaPlanAhorroProcessor)
            .pagarCuotas(any(List.class), eq(new BigDecimal("583333.34")), eq("pago multiple"), eq(null), eq(1L), eq("user"));
    }

    @Test
    void pagarCuotasMultiplesBloqueaCuotasDeDistintoContrato() {
        ContratoPlanAhorro contrato1 = contratoBase(99L);
        ContratoPlanAhorro contrato2 = contratoBase(100L);
        CuotaPlanAhorro cuota1 = cuotaConContrato(601L, 1, "1000.00", EstadoCuotaPlanAhorro.PENDIENTE, contrato1);
        CuotaPlanAhorro cuota2 = cuotaConContrato(602L, 1, "1000.00", EstadoCuotaPlanAhorro.PENDIENTE, contrato2);

        when(cuotaRepository.findOneByIdForUser(601L, "user")).thenReturn(Optional.of(cuota1));
        when(cuotaRepository.findOneByIdForUser(602L, "user")).thenReturn(Optional.of(cuota2));
        when(pagoCuotaPlanAhorroProcessor.pagarCuotas(any(List.class), eq(null), eq(null), eq(null), eq(null), eq("user")))
            .thenThrow(new BadRequestException("Todas las cuotas deben pertenecer al mismo contrato"));

        assertThrows(BadRequestException.class, () -> service.pagarCuotas(List.of(601L, 602L), null, null, null, null));
    }

    @Test
    void pagarCuotasMultiplesBloqueaCuotaPagada() {
        ContratoPlanAhorro contrato = contratoBase(99L);
        CuotaPlanAhorro cuota = cuotaConContrato(701L, 1, "1000.00", EstadoCuotaPlanAhorro.PAGADA, contrato);

        when(cuotaRepository.findOneByIdForUser(701L, "user")).thenReturn(Optional.of(cuota));
        when(pagoCuotaPlanAhorroProcessor.pagarCuotas(any(List.class), eq(null), eq(null), eq(null), eq(null), eq("user")))
            .thenThrow(new BadRequestException("Solo se pueden pagar cuotas pendientes o vencidas"));

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

