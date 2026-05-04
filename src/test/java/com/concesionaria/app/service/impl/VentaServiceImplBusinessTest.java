package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.Venta;
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
}

