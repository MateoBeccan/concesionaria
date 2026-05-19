package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.AdjudicacionPlanAhorro;
import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.domain.ReglaAdjudicacionPlan;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.domain.enumeration.EstadoAdjudicacionPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.domain.enumeration.TipoReglaAdjudicacionPlan;
import com.concesionaria.app.repository.AdjudicacionPlanAhorroRepository;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.PagoService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.AdjudicacionPlanAhorroDTO;
import com.concesionaria.app.service.dto.ElegibilidadAdjudicacionDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.VehiculoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ClienteMapperImpl;
import com.concesionaria.app.service.mapper.InventarioMapperImpl;
import com.concesionaria.app.service.mapper.VentaMapperImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AdjudicacionPlanAhorroServiceImplBusinessTest {

    @Mock
    private AdjudicacionPlanAhorroRepository adjudicacionRepository;

    @Mock
    private ContratoPlanAhorroRepository contratoRepository;

    @Mock
    private CuotaPlanAhorroRepository cuotaRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private VentaService ventaService;

    @Mock
    private PagoService pagoService;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private MetodoPagoRepository metodoPagoRepository;
    @Mock
    private VentaRepository ventaRepository;

    private AdjudicacionPlanAhorroServiceImpl service;

    @BeforeEach
    void setUp() {
        service =
            new AdjudicacionPlanAhorroServiceImpl(
                adjudicacionRepository,
                contratoRepository,
                cuotaRepository,
                inventarioRepository,
                new InventarioMapperImpl(),
                new ClienteMapperImpl(),
                new VentaMapperImpl(),
                ventaService,
                pagoService,
                pagoRepository,
                metodoPagoRepository,
                ventaRepository
            );
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN"));
    }

    @Test
    void adjudicarContratoValido() {
        ContratoPlanAhorro contrato = contratoBase(EstadoContratoPlanAhorro.ACTIVO);
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(adjudicacionRepository.existsByContratoIdAndEstadoIn(any(), any())).thenReturn(false);
        when(cuotaRepository.findAllByContratoIdOrderByNumeroCuotaAsc(1L)).thenReturn(List.of(cuotaPagada("100.00"), cuotaPagada("200.00")));
        when(adjudicacionRepository.save(any(AdjudicacionPlanAhorro.class))).thenAnswer(inv -> inv.getArgument(0));

        AdjudicacionPlanAhorroDTO result = service.adjudicarContrato(1L, "ok");

        assertThat(result.getEstado()).isEqualTo(EstadoAdjudicacionPlanAhorro.ADJUDICADA);
        assertThat(result.getMontoReconocidoCuotas()).isEqualByComparingTo("300.00");
        verify(contratoRepository).save(contrato);
    }

    @Test
    void noAdjudicarContratoCancelado() {
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contratoBase(EstadoContratoPlanAhorro.CANCELADO)));
        assertThrows(BadRequestException.class, () -> service.adjudicarContrato(1L, null));
    }

    @Test
    void finalizadoPuedeSerAptoSiNoHayRestriccion() {
        ContratoPlanAhorro contrato = contratoBase(EstadoContratoPlanAhorro.FINALIZADO);
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        ElegibilidadAdjudicacionDTO elegibilidad = service.evaluarElegibilidad(1L);
        assertThat(elegibilidad.isApto()).isTrue();
    }

    @Test
    void noAdjudicarDosVecesContratoActivo() {
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contratoBase(EstadoContratoPlanAhorro.ACTIVO)));
        when(adjudicacionRepository.existsByContratoIdAndEstadoIn(any(), any())).thenReturn(true);
        assertThrows(BadRequestException.class, () -> service.adjudicarContrato(1L, null));
    }

    @Test
    void noAsignarInventarioNoDisponible() {
        AdjudicacionPlanAhorro adjudicacion = adjudicacionBase();
        Inventario inventario = inventarioBase();
        inventario.setEstadoInventario(EstadoInventario.VENDIDO);
        when(adjudicacionRepository.findById(10L)).thenReturn(Optional.of(adjudicacion));
        when(inventarioRepository.findById(20L)).thenReturn(Optional.of(inventario));

        assertThrows(BadRequestException.class, () -> service.asignarInventario(10L, 20L));
    }

    @Test
    void generarVentaVinculadaYNoDuplicarEnReintento() {
        AdjudicacionPlanAhorro adjudicacion = adjudicacionBase();
        Inventario inventario = inventarioBase();
        adjudicacion.setInventario(inventario);
        adjudicacion.setVehiculo(inventario.getVehiculo());
        when(adjudicacionRepository.findById(10L)).thenReturn(Optional.of(adjudicacion));
        when(inventarioRepository.findById(20L)).thenReturn(Optional.of(inventario));
        when(adjudicacionRepository.save(any(AdjudicacionPlanAhorro.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoRepository.existsByVentaIdAndMetodoPagoCodigoAndEstado(500L, "PLAN_AHORRO", com.concesionaria.app.domain.enumeration.EstadoPago.REGISTRADO))
            .thenReturn(false);
        when(pagoRepository.sumMontoByVentaId(500L)).thenReturn(new BigDecimal("250.00"));
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId(999L);
        metodoPago.setCodigo("PLAN_AHORRO");
        when(metodoPagoRepository.findByCodigoIgnoreCase("PLAN_AHORRO")).thenReturn(Optional.of(metodoPago));

        VentaDTO ventaDTO = new VentaDTO();
        ventaDTO.setId(500L);
        ventaDTO.setEstado(EstadoVenta.PENDIENTE);
        ventaDTO.setTotal(new BigDecimal("1000.00"));
        MonedaDTO monedaDTO = new MonedaDTO();
        monedaDTO.setId(1L);
        monedaDTO.setCodigo("ARS");
        ventaDTO.setMoneda(monedaDTO);
        VehiculoDTO vehiculoDTO = new VehiculoDTO();
        vehiculoDTO.setId(30L);
        ventaDTO.setVehiculo(vehiculoDTO);
        when(ventaService.saveDesdePlanAhorro(any(VentaDTO.class))).thenReturn(ventaDTO);
        Venta ventaPersistida = new Venta();
        ventaPersistida.setId(500L);
        ventaPersistida.setTotal(new BigDecimal("1000.00"));
        when(ventaRepository.findById(500L)).thenReturn(Optional.of(ventaPersistida));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        AdjudicacionPlanAhorroDTO primera = service.generarVenta(10L);
        AdjudicacionPlanAhorroDTO segunda = service.generarVenta(10L);

        assertThat(primera.getVenta().getId()).isEqualTo(500L);
        assertThat(segunda.getVenta().getId()).isEqualTo(500L);
    }

    @Test
    void generarVentaConCreditoParcialQuedaPendienteConSaldo() {
        AdjudicacionPlanAhorro adjudicacion = adjudicacionBase();
        Inventario inventario = inventarioBase();
        adjudicacion.setInventario(inventario);
        adjudicacion.setVehiculo(inventario.getVehiculo());
        adjudicacion.setMontoReconocidoCuotas(new BigDecimal("250.00"));
        when(adjudicacionRepository.findById(10L)).thenReturn(Optional.of(adjudicacion));
        when(inventarioRepository.findById(20L)).thenReturn(Optional.of(inventario));
        when(adjudicacionRepository.save(any(AdjudicacionPlanAhorro.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoRepository.existsByVentaIdAndMetodoPagoCodigoAndEstado(500L, "PLAN_AHORRO", com.concesionaria.app.domain.enumeration.EstadoPago.REGISTRADO))
            .thenReturn(false);
        when(pagoRepository.sumMontoByVentaId(500L)).thenReturn(new BigDecimal("250.00"));
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId(999L);
        metodoPago.setCodigo("PLAN_AHORRO");
        when(metodoPagoRepository.findByCodigoIgnoreCase("PLAN_AHORRO")).thenReturn(Optional.of(metodoPago));

        VentaDTO ventaDTO = new VentaDTO();
        ventaDTO.setId(500L);
        ventaDTO.setEstado(EstadoVenta.PENDIENTE);
        ventaDTO.setTotal(new BigDecimal("1000.00"));
        MonedaDTO monedaDTO = new MonedaDTO();
        monedaDTO.setId(1L);
        monedaDTO.setCodigo("ARS");
        ventaDTO.setMoneda(monedaDTO);
        VehiculoDTO vehiculoDTO = new VehiculoDTO();
        vehiculoDTO.setId(30L);
        ventaDTO.setVehiculo(vehiculoDTO);
        when(ventaService.saveDesdePlanAhorro(any(VentaDTO.class))).thenReturn(ventaDTO);

        Venta ventaPersistida = new Venta();
        ventaPersistida.setId(500L);
        ventaPersistida.setTotal(new BigDecimal("1000.00"));
        when(ventaRepository.findById(500L)).thenReturn(Optional.of(ventaPersistida));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        service.generarVenta(10L);

        assertThat(ventaPersistida.getEstado()).isEqualTo(EstadoVenta.PENDIENTE);
        assertThat(ventaPersistida.getSaldo()).isEqualByComparingTo("750.00");
        assertThat(ventaPersistida.getTotalPagado()).isEqualByComparingTo("250.00");
    }

    @Test
    void generarVentaConCreditoTotalQuedaPagadaConSaldoCero() {
        AdjudicacionPlanAhorro adjudicacion = adjudicacionBase();
        Inventario inventario = inventarioBase();
        adjudicacion.setInventario(inventario);
        adjudicacion.setVehiculo(inventario.getVehiculo());
        adjudicacion.setMontoReconocidoCuotas(new BigDecimal("1000.00"));
        when(adjudicacionRepository.findById(10L)).thenReturn(Optional.of(adjudicacion));
        when(inventarioRepository.findById(20L)).thenReturn(Optional.of(inventario));
        when(adjudicacionRepository.save(any(AdjudicacionPlanAhorro.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoRepository.existsByVentaIdAndMetodoPagoCodigoAndEstado(500L, "PLAN_AHORRO", com.concesionaria.app.domain.enumeration.EstadoPago.REGISTRADO))
            .thenReturn(false);
        when(pagoRepository.sumMontoByVentaId(500L)).thenReturn(new BigDecimal("1000.00"));
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId(999L);
        metodoPago.setCodigo("PLAN_AHORRO");
        when(metodoPagoRepository.findByCodigoIgnoreCase("PLAN_AHORRO")).thenReturn(Optional.of(metodoPago));

        VentaDTO ventaDTO = new VentaDTO();
        ventaDTO.setId(500L);
        ventaDTO.setEstado(EstadoVenta.PENDIENTE);
        ventaDTO.setTotal(new BigDecimal("1000.00"));
        MonedaDTO monedaDTO = new MonedaDTO();
        monedaDTO.setId(1L);
        monedaDTO.setCodigo("ARS");
        ventaDTO.setMoneda(monedaDTO);
        VehiculoDTO vehiculoDTO = new VehiculoDTO();
        vehiculoDTO.setId(30L);
        ventaDTO.setVehiculo(vehiculoDTO);
        when(ventaService.saveDesdePlanAhorro(any(VentaDTO.class))).thenReturn(ventaDTO);

        Venta ventaPersistida = new Venta();
        ventaPersistida.setId(500L);
        ventaPersistida.setTotal(new BigDecimal("1000.00"));
        when(ventaRepository.findById(500L)).thenReturn(Optional.of(ventaPersistida));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        service.generarVenta(10L);

        assertThat(ventaPersistida.getEstado()).isEqualTo(EstadoVenta.PAGADA);
        assertThat(ventaPersistida.getSaldo()).isEqualByComparingTo("0.00");
        assertThat(ventaPersistida.getTotalPagado()).isEqualByComparingTo("1000.00");
        verify(ventaService).sincronizarInventarioConVenta(500L);
    }

    @Test
    void findAllComoAdminRetornaPaginaCompleta() {
        AdjudicacionPlanAhorro adjudicacion = adjudicacionBase();
        adjudicacion.setVehiculo(inventarioBase().getVehiculo());
        when(adjudicacionRepository.findAllWithEagerRelationships(PageRequest.of(0, 20))).thenReturn(new PageImpl<>(List.of(adjudicacion)));

        var result = service.findAll(PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNumeroContrato()).isEqualTo("PLAN-000001");
    }

    @Test
    void findAllCurrentUserFiltraPorLogin() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", "n/a", "ROLE_USER"));
        AdjudicacionPlanAhorro adjudicacion = adjudicacionBase();
        adjudicacion.setVehiculo(inventarioBase().getVehiculo());
        when(adjudicacionRepository.findAllByUserLoginWithEagerRelationships("user", PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(adjudicacion)));

        var result = service.findAllCurrentUser(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCliente().getNombre()).isEqualTo("Ana");
    }

    @Test
    void reglaPorCuotasNoAptaSiInsuficiente() {
        ContratoPlanAhorro contrato = contratoBase(EstadoContratoPlanAhorro.ACTIVO);
        contrato.setCuotasPagadas(10);
        contrato.getPlan().setReglaAdjudicacion(regla(TipoReglaAdjudicacionPlan.POR_CUOTAS, 24, null, false));
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        ElegibilidadAdjudicacionDTO result = service.evaluarElegibilidad(1L);
        assertThat(result.isApto()).isFalse();
    }

    @Test
    void reglaCuotasOPorcentajeAptaSiCumpleUna() {
        ContratoPlanAhorro contrato = contratoBase(EstadoContratoPlanAhorro.ACTIVO);
        contrato.setCuotasPagadas(10);
        contrato.setSaldoPendiente(new BigDecimal("600.00"));
        contrato.getPlan().setValorMovil(new BigDecimal("1000.00"));
        contrato.getPlan().setReglaAdjudicacion(regla(TipoReglaAdjudicacionPlan.CUOTAS_O_PORCENTAJE, 24, new BigDecimal("30.00"), false));
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        ElegibilidadAdjudicacionDTO result = service.evaluarElegibilidad(1L);
        assertThat(result.isApto()).isTrue();
    }

    @Test
    void reglaManualBloquea() {
        ContratoPlanAhorro contrato = contratoBase(EstadoContratoPlanAhorro.ACTIVO);
        contrato.getPlan().setReglaAdjudicacion(regla(TipoReglaAdjudicacionPlan.MANUAL, null, null, false));
        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        ElegibilidadAdjudicacionDTO result = service.evaluarElegibilidad(1L);
        assertThat(result.isApto()).isFalse();
    }

    private ContratoPlanAhorro contratoBase(EstadoContratoPlanAhorro estado) {
        Moneda moneda = new Moneda();
        moneda.setId(1L);
        moneda.setCodigo("ARS");
        Version version = new Version();
        version.setId(11L);

        PlanAhorro plan = new PlanAhorro();
        plan.setId(2L);
        plan.setMoneda(moneda);
        plan.setVersionObjetivo(version);

        Cliente cliente = new Cliente();
        cliente.setId(3L);
        cliente.setNombre("Ana");
        cliente.setApellido("Lopez");

        ContratoPlanAhorro contrato = new ContratoPlanAhorro();
        contrato.setId(1L);
        contrato.setNumeroContrato("PLAN-000001");
        contrato.setCliente(cliente);
        contrato.setPlan(plan);
        contrato.setEstado(estado);
        contrato.setSaldoPendiente(new BigDecimal("1000.00"));
        contrato.setCuotasPagadas(0);
        return contrato;
    }

    private ReglaAdjudicacionPlan regla(TipoReglaAdjudicacionPlan tipo, Integer minimoCuotas, BigDecimal minimoPorcentaje, boolean permiteMora) {
        ReglaAdjudicacionPlan regla = new ReglaAdjudicacionPlan();
        regla.setNombre("R-" + tipo);
        regla.setTipoRegla(tipo);
        regla.setMinimoCuotas(minimoCuotas);
        regla.setMinimoPorcentaje(minimoPorcentaje);
        regla.setPermiteMora(permiteMora);
        regla.setRequiereContratoActivo(true);
        regla.setActivo(true);
        return regla;
    }

    private CuotaPlanAhorro cuotaPagada(String importe) {
        CuotaPlanAhorro cuota = new CuotaPlanAhorro();
        cuota.setEstado(EstadoCuotaPlanAhorro.PAGADA);
        cuota.setImporte(new BigDecimal(importe));
        return cuota;
    }

    private Inventario inventarioBase() {
        Version version = new Version();
        version.setId(11L);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(30L);
        vehiculo.setPrecio(new BigDecimal("1000.00"));
        vehiculo.setVersion(version);

        Inventario inventario = new Inventario();
        inventario.setId(20L);
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setVehiculo(vehiculo);
        return inventario;
    }

    private AdjudicacionPlanAhorro adjudicacionBase() {
        AdjudicacionPlanAhorro adjudicacion = new AdjudicacionPlanAhorro();
        adjudicacion.setId(10L);
        adjudicacion.setContratoPlanAhorro(contratoBase(EstadoContratoPlanAhorro.ADJUDICADO));
        adjudicacion.setCliente(adjudicacion.getContratoPlanAhorro().getCliente());
        adjudicacion.setEstado(EstadoAdjudicacionPlanAhorro.ADJUDICADA);
        adjudicacion.setMontoReconocidoCuotas(new BigDecimal("250.00"));
        adjudicacion.setDiferenciaAPagar(new BigDecimal("750.00"));
        return adjudicacion;
    }
}
