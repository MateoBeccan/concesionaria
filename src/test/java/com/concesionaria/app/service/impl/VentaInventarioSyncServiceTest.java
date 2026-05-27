package com.concesionaria.app.service.impl;

import com.concesionaria.app.config.BusinessProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.InventarioHistorialRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.VentaHistorialRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.CurrencyConversionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VentaInventarioSyncServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private InventarioHistorialRepository inventarioHistorialRepository;
    @Mock private CurrencyConversionService currencyConversionService;
    @Mock private MonedaRepository monedaRepository;
    @Mock private PagoRepository pagoRepository;
    @Mock private VentaHistorialRepository ventaHistorialRepository;

    private VentaInventarioSyncService service;

    @BeforeEach
    void setUp() {
        VentaCalculator calculator = new VentaCalculator(
            currencyConversionService,
            monedaRepository,
            pagoRepository,
            BusinessProperties.defaults()
        );
        service =
            new VentaInventarioSyncService(
                ventaRepository,
                inventarioRepository,
                reservaRepository,
                inventarioHistorialRepository,
                calculator,
                new VentaStateManager(calculator),
                new VentaHistorialService(ventaHistorialRepository),
                BusinessProperties.defaults()
            );
    }

    @Test
    void ventaPendienteConMinimoReservaMarcaInventarioReservado() {
        Venta venta = ventaBase(1L, EstadoVenta.PENDIENTE, new BigDecimal("10000.00"), new BigDecimal("1000.00"));
        Inventario inventario = inventarioBase(10L, venta.getVehiculo().getId(), EstadoInventario.DISPONIBLE);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(inventarioRepository.findByVehiculoId(100L)).thenReturn(Optional.of(inventario));
        when(pagoRepository.sumMontoByVentaId(1L)).thenReturn(new BigDecimal("1000.00"));
        when(reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(10L, EstadoReserva.ACTIVA)).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        service.sincronizarConVenta(1L);

        assertThat(inventario.getEstadoInventario()).isEqualTo(EstadoInventario.RESERVADO);
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void ventaPagadaMarcaInventarioVendido() {
        Venta venta = ventaBase(2L, EstadoVenta.PAGADA, new BigDecimal("10000.00"), new BigDecimal("10000.00"));
        Inventario inventario = inventarioBase(20L, venta.getVehiculo().getId(), EstadoInventario.RESERVADO);

        when(ventaRepository.findById(2L)).thenReturn(Optional.of(venta));
        when(inventarioRepository.findByVehiculoId(200L)).thenReturn(Optional.of(inventario));
        when(pagoRepository.sumMontoByVentaId(2L)).thenReturn(new BigDecimal("10000.00"));
        when(reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(20L, EstadoReserva.ACTIVA)).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        service.sincronizarConVenta(2L);

        assertThat(inventario.getEstadoInventario()).isEqualTo(EstadoInventario.VENDIDO);
    }

    @Test
    void cancelacionLiberaInventarioSiNoEstaVendido() {
        Venta venta = ventaBase(3L, EstadoVenta.CANCELADA, new BigDecimal("10000.00"), new BigDecimal("0.00"));
        Inventario inventario = inventarioBase(30L, venta.getVehiculo().getId(), EstadoInventario.RESERVADO);

        when(ventaRepository.findById(3L)).thenReturn(Optional.of(venta));
        when(inventarioRepository.findByVehiculoId(300L)).thenReturn(Optional.of(inventario));
        when(reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(30L, EstadoReserva.ACTIVA)).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        service.sincronizarConVenta(3L);

        assertThat(inventario.getEstadoInventario()).isEqualTo(EstadoInventario.DISPONIBLE);
    }

    @Test
    void ventaPagadaNoDuplicaHistorialSiYaEstaVendido() {
        Venta venta = ventaBase(4L, EstadoVenta.PAGADA, new BigDecimal("10000.00"), new BigDecimal("10000.00"));
        Inventario inventario = inventarioBase(40L, venta.getVehiculo().getId(), EstadoInventario.VENDIDO);

        when(ventaRepository.findById(4L)).thenReturn(Optional.of(venta));
        when(inventarioRepository.findByVehiculoId(400L)).thenReturn(Optional.of(inventario));
        when(pagoRepository.sumMontoByVentaId(4L)).thenReturn(new BigDecimal("10000.00"));
        when(reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(40L, EstadoReserva.ACTIVA)).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        service.sincronizarConVenta(4L);

        verify(inventarioHistorialRepository, never()).save(any());
    }

    private Venta ventaBase(Long id, EstadoVenta estado, BigDecimal importeNeto, BigDecimal totalPagado) {
        Venta venta = new Venta();
        venta.setId(id);
        venta.setEstado(estado);
        venta.setImporteNeto(importeNeto);
        venta.setTotalPagado(totalPagado);
        venta.setCliente(new Cliente().id(id + 500));
        venta.setVehiculo(new Vehiculo().id(id * 100));
        return venta;
    }

    private Inventario inventarioBase(Long inventarioId, Long vehiculoId, EstadoInventario estadoInventario) {
        Inventario inventario = new Inventario();
        inventario.setId(inventarioId);
        inventario.setVehiculo(new Vehiculo().id(vehiculoId));
        inventario.setEstadoInventario(estadoInventario);
        inventario.setCreatedDate(Instant.now());
        return inventario;
    }
}

