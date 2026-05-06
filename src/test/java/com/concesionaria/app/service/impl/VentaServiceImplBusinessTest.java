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
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
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
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.VentaMapper;
import java.math.BigDecimal;
import java.time.Instant;
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
                currencyConversionService
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

