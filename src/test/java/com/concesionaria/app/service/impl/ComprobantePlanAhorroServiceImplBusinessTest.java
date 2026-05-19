package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.ComprobantePlanAhorro;
import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.repository.ComprobantePlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.service.dto.ComprobantePdfResult;
import com.concesionaria.app.service.dto.ComprobantePlanAhorroDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ComprobantePlanAhorroServiceImplBusinessTest {

    @Mock
    private ComprobantePlanAhorroRepository repository;

    @Mock
    private CuotaPlanAhorroRepository cuotaRepository;

    private ComprobantePlanAhorroServiceImpl service;

    @BeforeEach
    void setUp() {
        service =
            new ComprobantePlanAhorroServiceImpl(
                repository,
                cuotaRepository,
                "Concesionaria Test",
                "Calle Test 123",
                "+54 11 0000-0000",
                "test@concesionaria.local",
                "30-00000000-0",
                "src/main/webapp/content/images/branding/logo.png"
            );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pagarCuotaEmiteComprobanteConNumeracionAutomatica() {
        setUser("user", "ROLE_USER");
        CuotaPlanAhorro cuota = cuotaBase(101L, 3, "50000.00");
        Pago pago = new Pago();
        pago.setId(901L);

        when(repository.existsByCuotaPlanAhorroIdAndEstado(101L, EstadoComprobante.EMITIDO)).thenReturn(false);
        when(repository.nextCorrelativoBase()).thenReturn(7L);
        when(repository.save(any(ComprobantePlanAhorro.class))).thenAnswer(inv -> {
            ComprobantePlanAhorro entity = inv.getArgument(0);
            entity.setId(801L);
            return entity;
        });

        ComprobantePlanAhorroDTO dto = service.emitirParaCuota(cuota, pago);

        assertThat(dto.getId()).isEqualTo(801L);
        assertThat(dto.getNumeroComprobante()).isEqualTo("CPA-000008");
        assertThat(dto.getEstado()).isEqualTo(EstadoComprobante.EMITIDO);
        assertThat(dto.getPagoId()).isEqualTo(901L);
        assertThat(dto.getCuotaPlanAhorroId()).isEqualTo(101L);
        assertThat(dto.getUsuarioEmision()).isEqualTo("user");
    }

    @Test
    void noDuplicaComprobanteActivoPorCuota() {
        setUser("user", "ROLE_USER");
        CuotaPlanAhorro cuota = cuotaBase(102L, 4, "50000.00");
        Pago pago = new Pago();
        pago.setId(902L);

        when(repository.existsByCuotaPlanAhorroIdAndEstado(102L, EstadoComprobante.EMITIDO)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.emitirParaCuota(cuota, pago));
        assertThat(ex.getMessage()).contains("ya tiene un comprobante activo");
        verify(repository, never()).save(any());
    }

    @Test
    void anularPagoAnulaComprobanteActivo() {
        ComprobantePlanAhorro emitido = comprobanteBase(777L, 909L, EstadoComprobante.EMITIDO);
        Instant fecha = Instant.parse("2026-05-13T12:00:00Z");

        when(repository.findFirstByPagoIdAndEstadoOrderByIdDesc(909L, EstadoComprobante.EMITIDO)).thenReturn(Optional.of(emitido));
        when(repository.save(any(ComprobantePlanAhorro.class))).thenAnswer(inv -> inv.getArgument(0));

        service.anularPorPago(909L, "reversa", "admin", fecha);

        assertThat(emitido.getEstado()).isEqualTo(EstadoComprobante.ANULADO);
        assertThat(emitido.getUsuarioAnulacion()).isEqualTo("admin");
        assertThat(emitido.getFechaAnulacion()).isEqualTo(fecha);
        assertThat(emitido.getMotivoAnulacion()).contains("Anulado por anulaci");
        verify(repository).save(emitido);
    }

    @Test
    void generarPdfDevuelveBytes() {
        setUser("admin", "ROLE_ADMIN");
        ComprobantePlanAhorro entity = comprobanteBase(778L, 910L, EstadoComprobante.EMITIDO);
        when(repository.findById(778L)).thenReturn(Optional.of(entity));

        Optional<ComprobantePdfResult> result = service.generarPdf(778L);

        assertThat(result).isPresent();
        assertThat(result.get().content()).isNotEmpty();
        assertThat(result.get().fileName()).isEqualTo("CPA-000010.pdf");
    }

    @Test
    void usuarioSinPermisoNoAccedeCuota() {
        setUser("user", "ROLE_USER");
        when(cuotaRepository.findOneByIdForUser(222L, "user")).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.findByCuota(222L));
    }

    @Test
    void usuarioConPermisoPuedeVerComprobantesDeCuota() {
        setUser("user", "ROLE_USER");
        ComprobantePlanAhorro entity = comprobanteBase(900L, 999L, EstadoComprobante.EMITIDO);
        when(cuotaRepository.findOneByIdForUser(333L, "user")).thenReturn(Optional.of(new CuotaPlanAhorro()));
        when(repository.findAllByCuotaPlanAhorroIdOrderByIdDesc(333L)).thenReturn(List.of(entity));

        List<ComprobantePlanAhorroDTO> result = service.findByCuota(333L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNumeroComprobante()).isEqualTo("CPA-000010");
    }

    private void setUser(String login, String authority) {
        SecurityContextHolder
            .getContext()
            .setAuthentication(new TestingAuthenticationToken(login, "n/a", authority));
    }

    private CuotaPlanAhorro cuotaBase(Long cuotaId, int numeroCuota, String importe) {
        Moneda moneda = new Moneda();
        moneda.setId(1L);
        moneda.setCodigo("ARS");
        moneda.setDescripcion("Peso argentino");
        moneda.setSimbolo("$");

        PlanAhorro plan = new PlanAhorro();
        plan.setId(10L);
        plan.setNombre("Plan Corolla 84");
        plan.setMoneda(moneda);

        Cliente cliente = new Cliente();
        cliente.setId(20L);
        cliente.setNombre("Ana");
        cliente.setApellido("Lopez");

        ContratoPlanAhorro contrato = new ContratoPlanAhorro();
        contrato.setId(30L);
        contrato.setNumeroContrato("CPA-CON-0001");
        contrato.setCliente(cliente);
        contrato.setPlan(plan);

        CuotaPlanAhorro cuota = new CuotaPlanAhorro();
        cuota.setId(cuotaId);
        cuota.setNumeroCuota(numeroCuota);
        cuota.setImporte(new BigDecimal(importe));
        cuota.setContrato(contrato);
        return cuota;
    }

    private ComprobantePlanAhorro comprobanteBase(Long comprobanteId, Long pagoId, EstadoComprobante estado) {
        CuotaPlanAhorro cuota = cuotaBase(501L, 5, "75000.00");
        Pago pago = new Pago();
        pago.setId(pagoId);

        ComprobantePlanAhorro entity = new ComprobantePlanAhorro();
        entity.setId(comprobanteId);
        entity.setContratoPlanAhorro(cuota.getContrato());
        entity.setCuotaPlanAhorro(cuota);
        entity.setPago(pago);
        entity.setNumeroComprobante("CPA-000010");
        entity.setFechaEmision(Instant.now());
        entity.setImporte(cuota.getImporte());
        entity.setMoneda(cuota.getContrato().getPlan().getMoneda());
        entity.setEstado(estado);
        entity.setUsuarioEmision("admin");
        return entity;
    }
}
