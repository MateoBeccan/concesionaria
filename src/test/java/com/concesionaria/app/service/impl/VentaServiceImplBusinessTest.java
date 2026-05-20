package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.repository.InventarioHistorialRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.TasacionUsadoRepository;
import com.concesionaria.app.repository.UserRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.repository.VentaHistorialRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.CurrencyConversionService;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.CotizacionConversionDTO;
import com.concesionaria.app.service.dto.ReservaDTO;
import com.concesionaria.app.service.dto.VehiculoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.VentaMapper;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplBusinessTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private VentaMapper ventaMapper;
    @Mock private VehiculoRepository vehiculoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private InventarioHistorialRepository inventarioHistorialRepository;
    @Mock private PagoRepository pagoRepository;
    @Mock private MonedaRepository monedaRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private TasacionUsadoRepository tasacionUsadoRepository;
    @Mock private VentaHistorialRepository ventaHistorialRepository;
    @Mock private CurrencyConversionService currencyConversionService;

    private VentaServiceImpl service;

    @BeforeEach
    void setUp() {
        service =
            new VentaServiceImpl(
                ventaRepository,
                ventaMapper,
                vehiculoRepository,
                inventarioRepository,
                clienteRepository,
                inventarioHistorialRepository,
                pagoRepository,
                monedaRepository,
                userRepository,
                reservaRepository,
                tasacionUsadoRepository,
                ventaHistorialRepository,
                currencyConversionService,
                new VentaValidator(ventaRepository, reservaRepository, inventarioRepository, vehiculoRepository),
                new VentaCalculator(currencyConversionService, monedaRepository, pagoRepository),
                new VentaStateManager(new VentaCalculator(currencyConversionService, monedaRepository, pagoRepository)),
                new VentaHistorialService(ventaHistorialRepository),
                new VentaInventarioSyncService()
            );
        ReflectionTestUtils.setField(service, "monedaBaseCodigo", "ARS");
    }

    @Test
    void bloqueaIngresoUsadoSiFaltaKm() {
        Moneda ars = new Moneda().id(1L);
        ars.setCodigo("ARS");

        Venta venta = new Venta();
        venta.setMoneda(ars);
        venta.setCliente(new Cliente().id(10L));

        TasacionUsado tasacion = new TasacionUsado();
        tasacion.setMontoTasacion(new BigDecimal("10000"));
        tasacion.setAnioUsado(2020);
        tasacion.setKmUsado(null);
        tasacion.setVersion(new com.concesionaria.app.domain.Version().id(8L));
        tasacion.setTipoVehiculo(new com.concesionaria.app.domain.TipoVehiculo().id(3L));

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> ReflectionTestUtils.invokeMethod(service, "validarTasacionParaIngresoInventario", venta, tasacion)
        );
        assertThat(ex.getMessage()).contains("kilometraje");
    }

    @Test
    void bloqueaIngresoUsadoSiFaltaVersionTecnica() {
        Moneda ars = new Moneda().id(1L);
        ars.setCodigo("ARS");

        Venta venta = new Venta();
        venta.setMoneda(ars);
        venta.setCliente(new Cliente().id(10L));

        TasacionUsado tasacion = new TasacionUsado();
        tasacion.setMontoTasacion(new BigDecimal("10000"));
        tasacion.setAnioUsado(2020);
        tasacion.setKmUsado(1000);
        tasacion.setTipoVehiculo(new com.concesionaria.app.domain.TipoVehiculo().id(3L));

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> ReflectionTestUtils.invokeMethod(service, "validarTasacionParaIngresoInventario", venta, tasacion)
        );
        assertThat(ex.getMessage()).contains("version");
    }

    @Test
    void ventaPagadaGeneraVehiculoEInventarioParaTomaUsado() {
        Moneda ars = moneda(1L, "ARS");
        Venta venta = ventaBase(200L, EstadoVenta.RESERVADA, ars, new BigDecimal("0.00"));
        TasacionUsado tasacion = tasacionCompleta(300L, ars);
        venta.setTasacionUsado(tasacion);

        Inventario inventarioVenta = new Inventario();
        inventarioVenta.setId(500L);
        inventarioVenta.setEstadoInventario(EstadoInventario.RESERVADO);
        inventarioVenta.setVehiculo(venta.getVehiculo());

        when(ventaRepository.findById(200L)).thenReturn(java.util.Optional.of(venta));
        when(inventarioRepository.findByVehiculoId(900L)).thenReturn(java.util.Optional.of(inventarioVenta));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> {
            Inventario i = inv.getArgument(0);
            if (i.getId() == null) {
                i.setId(700L);
            }
            return i;
        });
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tasacionUsadoRepository.findById(300L)).thenReturn(java.util.Optional.of(tasacion));
        when(tasacionUsadoRepository.save(any(TasacionUsado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(monedaRepository.findByCodigoIgnoreCase("ARS")).thenReturn(java.util.Optional.of(ars));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> {
            Vehiculo v = inv.getArgument(0);
            if (v.getId() == null) {
                v.setId(600L);
            }
            return v;
        });

        service.confirmarVenta(200L);

        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
        verify(tasacionUsadoRepository, times(1)).save(any(TasacionUsado.class));
        assertThat(tasacion.getInventarioGenerado()).isNotNull();
        assertThat(tasacion.getInventarioGenerado().getId()).isEqualTo(700L);
    }

    @Test
    void confirmarVentaDosVecesNoDuplicaInventarioUsado() {
        Moneda ars = moneda(1L, "ARS");
        Venta venta = ventaBase(201L, EstadoVenta.PAGADA, ars, new BigDecimal("0.00"));
        TasacionUsado tasacion = tasacionCompleta(301L, ars);
        venta.setTasacionUsado(tasacion);

        Inventario inventarioVenta = new Inventario();
        inventarioVenta.setId(501L);
        inventarioVenta.setEstadoInventario(EstadoInventario.RESERVADO);
        inventarioVenta.setVehiculo(venta.getVehiculo());

        when(ventaRepository.findById(201L)).thenReturn(java.util.Optional.of(venta));
        when(inventarioRepository.findByVehiculoId(901L)).thenReturn(java.util.Optional.of(inventarioVenta));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> {
            Inventario i = inv.getArgument(0);
            if (i.getId() == null) {
                i.setId(701L);
            }
            return i;
        });
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tasacionUsadoRepository.findById(301L)).thenReturn(java.util.Optional.of(tasacion));
        when(tasacionUsadoRepository.save(any(TasacionUsado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(monedaRepository.findByCodigoIgnoreCase("ARS")).thenReturn(java.util.Optional.of(ars));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> {
            Vehiculo v = inv.getArgument(0);
            if (v.getId() == null) {
                v.setId(601L);
            }
            return v;
        });

        service.confirmarVenta(201L);
        service.confirmarVenta(201L);

        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
    }

    @Test
    void ventaPagadaRecuperaTasacionDesdePagoEntregaUsadoSiVentaNoLaTiene() {
        Moneda ars = moneda(1L, "ARS");
        Venta venta = ventaBase(204L, EstadoVenta.RESERVADA, ars, new BigDecimal("0.00"));
        venta.setTasacionUsado(null);

        TasacionUsado tasacion = tasacionCompleta(304L, ars);
        Pago pagoEntregaUsado = new Pago();
        pagoEntregaUsado.setId(804L);
        pagoEntregaUsado.setEstado(EstadoPago.REGISTRADO);
        pagoEntregaUsado.setTipoMovimiento(TipoMovimientoPago.ENTREGA_USADO);
        pagoEntregaUsado.setTasacionUsado(tasacion);
        pagoEntregaUsado.setVenta(venta);

        Inventario inventarioVenta = new Inventario();
        inventarioVenta.setId(504L);
        inventarioVenta.setEstadoInventario(EstadoInventario.RESERVADO);
        inventarioVenta.setVehiculo(venta.getVehiculo());

        when(ventaRepository.findById(204L)).thenReturn(java.util.Optional.of(venta));
        when(inventarioRepository.findByVehiculoId(904L)).thenReturn(java.util.Optional.of(inventarioVenta));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> {
            Inventario i = inv.getArgument(0);
            if (i.getId() == null) {
                i.setId(704L);
            }
            return i;
        });
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(monedaRepository.findByCodigoIgnoreCase("ARS")).thenReturn(java.util.Optional.of(ars));
        when(tasacionUsadoRepository.findById(304L)).thenReturn(java.util.Optional.of(tasacion));
        when(tasacionUsadoRepository.save(any(TasacionUsado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(
            pagoRepository.findFirstByVentaIdAndEstadoAndTipoMovimientoAndTasacionUsadoIsNotNullOrderByFechaDescIdDesc(
                204L,
                EstadoPago.REGISTRADO,
                TipoMovimientoPago.ENTREGA_USADO
            )
        )
            .thenReturn(java.util.Optional.of(pagoEntregaUsado));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> {
            Vehiculo v = inv.getArgument(0);
            if (v.getId() == null) {
                v.setId(604L);
            }
            return v;
        });

        service.confirmarVenta(204L);

        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
        assertThat(venta.getTasacionUsado()).isNotNull();
        assertThat(venta.getTasacionUsado().getId()).isEqualTo(304L);
    }

    @Test
    void ventaCanceladaNoGeneraInventarioUsadoAlConfirmar() {
        Moneda ars = moneda(1L, "ARS");
        Venta venta = ventaBase(202L, EstadoVenta.CANCELADA, ars, new BigDecimal("0.00"));
        venta.setTasacionUsado(tasacionCompleta(302L, ars));

        when(ventaRepository.findById(202L)).thenReturn(java.util.Optional.of(venta));

        assertThrows(BadRequestException.class, () -> service.confirmarVenta(202L));
        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }

    @Test
    void ventaReservadaNoGeneraInventarioUsadoSoloPorSincronizar() {
        Moneda ars = moneda(1L, "ARS");
        Venta venta = ventaBase(203L, EstadoVenta.RESERVADA, ars, new BigDecimal("1000.00"));
        venta.setTotal(new BigDecimal("10000.00"));
        venta.setTotalPagado(new BigDecimal("1000.00"));
        venta.setTasacionUsado(tasacionCompleta(303L, ars));

        Inventario inventarioVenta = new Inventario();
        inventarioVenta.setId(503L);
        inventarioVenta.setEstadoInventario(EstadoInventario.RESERVADO);
        inventarioVenta.setVehiculo(venta.getVehiculo());

        when(ventaRepository.findById(203L)).thenReturn(java.util.Optional.of(venta));
        when(inventarioRepository.findByVehiculoId(903L)).thenReturn(java.util.Optional.of(inventarioVenta));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoRepository.sumMontoByVentaId(203L)).thenReturn(new BigDecimal("1000.00"));

        service.sincronizarInventarioConVenta(203L);

        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }

    @Test
    void ventaFinalizadaConSaldoCeroGeneraInventarioUsadoAlSincronizar() {
        Moneda ars = moneda(1L, "ARS");
        Venta venta = ventaBase(205L, EstadoVenta.FINALIZADA, ars, BigDecimal.ZERO);
        TasacionUsado tasacion = tasacionCompleta(305L, ars);
        venta.setTasacionUsado(tasacion);

        Inventario inventarioVenta = new Inventario();
        inventarioVenta.setId(505L);
        inventarioVenta.setEstadoInventario(EstadoInventario.RESERVADO);
        inventarioVenta.setVehiculo(venta.getVehiculo());

        when(ventaRepository.findById(205L)).thenReturn(java.util.Optional.of(venta));
        when(inventarioRepository.findByVehiculoId(905L)).thenReturn(java.util.Optional.of(inventarioVenta));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> {
            Inventario i = inv.getArgument(0);
            if (i.getId() == null) {
                i.setId(705L);
            }
            return i;
        });
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tasacionUsadoRepository.findById(305L)).thenReturn(java.util.Optional.of(tasacion));
        when(tasacionUsadoRepository.save(any(TasacionUsado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(monedaRepository.findByCodigoIgnoreCase("ARS")).thenReturn(java.util.Optional.of(ars));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> {
            Vehiculo v = inv.getArgument(0);
            if (v.getId() == null) {
                v.setId(605L);
            }
            return v;
        });

        service.sincronizarInventarioConVenta(205L);

        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
        verify(tasacionUsadoRepository, times(1)).save(any(TasacionUsado.class));
        assertThat(tasacion.getInventarioGenerado()).isNotNull();
        assertThat(tasacion.getInventarioGenerado().getId()).isEqualTo(705L);
    }

    @Test
    void validarVentaDtoBloqueaVehiculoReservadoPorOtraOperacion() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(999L);
        vehiculo.setMoneda(moneda(1L, "ARS"));
        vehiculo.setPrecio(new BigDecimal("24500000.00"));

        Inventario inventario = new Inventario();
        inventario.setId(300L);
        inventario.setVehiculo(vehiculo);
        inventario.setEstadoInventario(EstadoInventario.RESERVADO);

        Cliente clienteReserva = new Cliente().id(77L);
        Reserva reservaActiva = new Reserva();
        reservaActiva.setId(901L);
        reservaActiva.setCliente(clienteReserva);
        reservaActiva.setEstado(EstadoReserva.ACTIVA);
        reservaActiva.setFechaVencimiento(Instant.now().plusSeconds(3600));

        when(vehiculoRepository.findById(999L)).thenReturn(java.util.Optional.of(vehiculo));
        when(inventarioRepository.findByVehiculoId(999L)).thenReturn(java.util.Optional.of(inventario));
        when(reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(300L, EstadoReserva.ACTIVA))
            .thenReturn(java.util.Optional.of(reservaActiva));

        VentaDTO dto = new VentaDTO();
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setId(10L);
        dto.setCliente(clienteDTO);
        VehiculoDTO vehiculoDTO = new VehiculoDTO();
        vehiculoDTO.setId(999L);
        dto.setVehiculo(vehiculoDTO);
        dto.setEstado(EstadoVenta.PENDIENTE);
        dto.setFecha(Instant.now());
        dto.setTotalPagado(new BigDecimal("3000000.00"));
        ReservaDTO reservaDTO = new ReservaDTO();
        reservaDTO.setId(9999L);
        dto.setReserva(reservaDTO);

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> ReflectionTestUtils.invokeMethod(service, "validarVentaDto", dto, true)
        );
        assertThat(ex.getMessage()).contains("reservado por otra operacion activa");
    }

    @Test
    void validarVentaDtoPermiteVehiculoReservadoCuandoEsLaMismaVenta() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1001L);
        vehiculo.setMoneda(moneda(1L, "ARS"));
        vehiculo.setPrecio(new BigDecimal("24500000.00"));

        Inventario inventario = new Inventario();
        inventario.setId(301L);
        inventario.setVehiculo(vehiculo);
        inventario.setEstadoInventario(EstadoInventario.RESERVADO);

        Cliente clienteReserva = new Cliente().id(77L);
        Reserva reservaActiva = new Reserva();
        reservaActiva.setId(902L);
        reservaActiva.setCliente(clienteReserva);
        reservaActiva.setEstado(EstadoReserva.ACTIVA);
        reservaActiva.setFechaVencimiento(Instant.now().plusSeconds(3600));

        Venta ventaExistente = new Venta();
        ventaExistente.setId(20L);
        ventaExistente.setReserva(reservaActiva);

        when(vehiculoRepository.findById(1001L)).thenReturn(java.util.Optional.of(vehiculo));
        when(inventarioRepository.findByVehiculoId(1001L)).thenReturn(java.util.Optional.of(inventario));
        when(reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(301L, EstadoReserva.ACTIVA))
            .thenReturn(java.util.Optional.of(reservaActiva));
        when(ventaRepository.findById(20L)).thenReturn(java.util.Optional.of(ventaExistente));
        when(monedaRepository.findByCodigoIgnoreCase("ARS")).thenReturn(java.util.Optional.of(moneda(1L, "ARS")));
        when(ventaRepository.existsByVehiculoIdAndEstadoInAndIdNot(any(), any(), any())).thenReturn(false);
        User user = new User();
        user.setId(501L);
        when(userRepository.findById(501L)).thenReturn(java.util.Optional.of(user));

        CotizacionConversionDTO conversionDTO = new CotizacionConversionDTO();
        conversionDTO.setMontoConvertido(new BigDecimal("24500000.00"));
        conversionDTO.setCotizacionAplicada(BigDecimal.ONE);
        conversionDTO.setFechaCotizacionUsada(Instant.now());
        conversionDTO.setCotizacionOrigenId(null);
        when(currencyConversionService.convertir(any(), any(), any(), any())).thenReturn(conversionDTO);

        VentaDTO dto = new VentaDTO();
        dto.setId(20L);
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setId(77L);
        dto.setCliente(clienteDTO);
        VehiculoDTO vehiculoDTO = new VehiculoDTO();
        vehiculoDTO.setId(1001L);
        dto.setVehiculo(vehiculoDTO);
        dto.setEstado(EstadoVenta.RESERVADA);
        dto.setFecha(Instant.now());
        dto.setTotalPagado(new BigDecimal("3000000.00"));
        dto.setPorcentajeImpuesto(new BigDecimal("21.00"));
        dto.setUser(new com.concesionaria.app.service.dto.UserDTO());
        dto.getUser().setId(501L);

        Assertions.assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(service, "validarVentaDto", dto, true));
    }

    @Test
    void validarVentaDtoPermiteVehiculoReservadoSiLaVentaExistenteEsLaMismaAunqueNoTengaReservaAsociada() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1002L);
        vehiculo.setMoneda(moneda(1L, "ARS"));
        vehiculo.setPrecio(new BigDecimal("24500000.00"));

        Inventario inventario = new Inventario();
        inventario.setId(302L);
        inventario.setVehiculo(vehiculo);
        inventario.setEstadoInventario(EstadoInventario.RESERVADO);

        Cliente clienteReserva = new Cliente().id(88L);
        Reserva reservaActiva = new Reserva();
        reservaActiva.setId(903L);
        reservaActiva.setCliente(clienteReserva);
        reservaActiva.setInventario(inventario);
        reservaActiva.setEstado(EstadoReserva.ACTIVA);
        reservaActiva.setFechaVencimiento(Instant.now().plusSeconds(3600));

        Venta ventaExistente = new Venta();
        ventaExistente.setId(21L);
        ventaExistente.setVehiculo(vehiculo);
        ventaExistente.setReserva(null);

        when(vehiculoRepository.findById(1002L)).thenReturn(java.util.Optional.of(vehiculo));
        when(inventarioRepository.findByVehiculoId(1002L)).thenReturn(java.util.Optional.of(inventario));
        when(reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(302L, EstadoReserva.ACTIVA))
            .thenReturn(java.util.Optional.of(reservaActiva));
        when(ventaRepository.findById(21L)).thenReturn(java.util.Optional.of(ventaExistente));
        when(monedaRepository.findByCodigoIgnoreCase("ARS")).thenReturn(java.util.Optional.of(moneda(1L, "ARS")));
        when(ventaRepository.existsByVehiculoIdAndEstadoInAndIdNot(any(), any(), any())).thenReturn(false);
        User user = new User();
        user.setId(502L);
        when(userRepository.findById(502L)).thenReturn(java.util.Optional.of(user));

        CotizacionConversionDTO conversionDTO = new CotizacionConversionDTO();
        conversionDTO.setMontoConvertido(new BigDecimal("24500000.00"));
        conversionDTO.setCotizacionAplicada(BigDecimal.ONE);
        conversionDTO.setFechaCotizacionUsada(Instant.now());
        conversionDTO.setCotizacionOrigenId(null);
        when(currencyConversionService.convertir(any(), any(), any(), any())).thenReturn(conversionDTO);

        VentaDTO dto = new VentaDTO();
        dto.setId(21L);
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setId(88L);
        dto.setCliente(clienteDTO);
        VehiculoDTO vehiculoDTO = new VehiculoDTO();
        vehiculoDTO.setId(1002L);
        dto.setVehiculo(vehiculoDTO);
        dto.setEstado(EstadoVenta.PAGADA);
        dto.setFecha(Instant.now());
        dto.setTotalPagado(new BigDecimal("29645000.00"));
        dto.setSaldo(BigDecimal.ZERO);
        dto.setPorcentajeImpuesto(new BigDecimal("21.00"));
        dto.setUser(new com.concesionaria.app.service.dto.UserDTO());
        dto.getUser().setId(502L);

        Assertions.assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(service, "validarVentaDto", dto, true));
    }

    @Test
    void ventaTradicionalSinAnticipoMinimoSigueBloqueada() {
        VentaDTO dto = construirVentaDtoParaValidacion(111L, 501L, BigDecimal.ZERO);
        mockValidacionesBaseVenta(111L, 501L);

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> ReflectionTestUtils.invokeMethod(service, "validarVentaDto", dto, true)
        );
        assertThat(ex.getMessage()).contains("pago minimo");
    }

    @Test
    void ventaDesdePlanAhorroPermiteAltaSinAnticipoTradicional() {
        VentaDTO dto = construirVentaDtoParaValidacion(112L, 502L, BigDecimal.ZERO);
        mockValidacionesBaseVenta(112L, 502L);

        Assertions.assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(service, "validarVentaDto", dto, false));
    }

    private VentaDTO construirVentaDtoParaValidacion(Long vehiculoId, Long userId, BigDecimal totalPagado) {
        VentaDTO dto = new VentaDTO();
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setId(10L);
        dto.setCliente(clienteDTO);
        VehiculoDTO vehiculoDTO = new VehiculoDTO();
        vehiculoDTO.setId(vehiculoId);
        dto.setVehiculo(vehiculoDTO);
        dto.setEstado(EstadoVenta.PENDIENTE);
        dto.setFecha(Instant.now());
        dto.setTotalPagado(totalPagado);
        dto.setPorcentajeImpuesto(new BigDecimal("21.00"));
        dto.setUser(new com.concesionaria.app.service.dto.UserDTO());
        dto.getUser().setId(userId);
        return dto;
    }

    private void mockValidacionesBaseVenta(Long vehiculoId, Long userId) {
        Moneda ars = moneda(1L, "ARS");
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(vehiculoId);
        vehiculo.setMoneda(ars);
        vehiculo.setPrecio(new BigDecimal("24500000.00"));

        Inventario inventario = new Inventario();
        inventario.setId(vehiculoId + 200L);
        inventario.setVehiculo(vehiculo);
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);

        User user = new User();
        user.setId(userId);

        when(vehiculoRepository.findById(vehiculoId)).thenReturn(java.util.Optional.of(vehiculo));
        when(inventarioRepository.findByVehiculoId(vehiculoId)).thenReturn(java.util.Optional.of(inventario));
        when(monedaRepository.findByCodigoIgnoreCase("ARS")).thenReturn(java.util.Optional.of(ars));
        when(ventaRepository.existsByVehiculoIdAndEstadoIn(any(), any())).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

        CotizacionConversionDTO conversionDTO = new CotizacionConversionDTO();
        conversionDTO.setMontoConvertido(new BigDecimal("24500000.00"));
        conversionDTO.setCotizacionAplicada(BigDecimal.ONE);
        conversionDTO.setFechaCotizacionUsada(Instant.now());
        conversionDTO.setCotizacionOrigenId(null);
        when(currencyConversionService.convertir(any(), any(), any(), any())).thenReturn(conversionDTO);
    }

    private Moneda moneda(Long id, String codigo) {
        Moneda moneda = new Moneda();
        moneda.setId(id);
        moneda.setCodigo(codigo);
        return moneda;
    }

    private Venta ventaBase(Long id, EstadoVenta estado, Moneda moneda, BigDecimal saldo) {
        Venta venta = new Venta();
        venta.setId(id);
        venta.setEstado(estado);
        venta.setSaldo(saldo);
        venta.setMoneda(moneda);
        venta.setCliente(new Cliente().id(10L));
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(id + 700L);
        venta.setVehiculo(vehiculo);
        venta.setTotal(new BigDecimal("10000.00"));
        venta.setTotalPagado(new BigDecimal("10000.00").subtract(saldo));
        return venta;
    }

    private TasacionUsado tasacionCompleta(Long id, Moneda moneda) {
        TasacionUsado tasacion = new TasacionUsado();
        tasacion.setId(id);
        tasacion.setMontoTasacion(new BigDecimal("5000.00"));
        tasacion.setAnioUsado(2020);
        tasacion.setKmUsado(120000);
        tasacion.setPatenteUsado("AB123CD");
        tasacion.setVinChasisUsado("VIN1234567890");
        tasacion.setColorUsado("Gris");
        tasacion.setFechaTasacion(Instant.now());
        tasacion.setMoneda(moneda);
        tasacion.setVersion(new com.concesionaria.app.domain.Version().id(8L));
        tasacion.setTipoVehiculo(new com.concesionaria.app.domain.TipoVehiculo().id(3L));
        return tasacion;
    }
}

