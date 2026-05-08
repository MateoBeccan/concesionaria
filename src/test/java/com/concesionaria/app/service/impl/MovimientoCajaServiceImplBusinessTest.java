package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.MovimientoCaja;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.repository.MovimientoCajaRepository;
import com.concesionaria.app.service.dto.ResumenDiarioCajaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.MovimientoCajaMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MovimientoCajaServiceImplBusinessTest {

    @Mock
    private MovimientoCajaRepository movimientoCajaRepository;

    @Mock
    private MovimientoCajaMapper movimientoCajaMapper;

    private MovimientoCajaServiceImpl movimientoCajaService;

    @BeforeEach
    void setUp() {
        movimientoCajaService = new MovimientoCajaServiceImpl(movimientoCajaRepository, movimientoCajaMapper);
    }

    @Test
    void pagoMonetarioGeneraIngreso() {
        Pago pago = pagoBase(101L, "2500000.00", "2500000.00", "admin");
        when(movimientoCajaRepository.existsByPagoIdAndTipoMovimiento(101L, TipoMovimientoCaja.INGRESO)).thenReturn(false);
        when(movimientoCajaRepository.save(any(MovimientoCaja.class))).thenAnswer(inv -> inv.getArgument(0));

        movimientoCajaService.registrarDesdePago(pago, TipoMovimientoCaja.INGRESO, EstadoPago.REGISTRADO, true);

        ArgumentCaptor<MovimientoCaja> captor = ArgumentCaptor.forClass(MovimientoCaja.class);
        verify(movimientoCajaRepository).save(captor.capture());
        MovimientoCaja mov = captor.getValue();
        assertThat(mov.getTipoMovimiento()).isEqualTo(TipoMovimientoCaja.INGRESO);
        assertThat(mov.getEstado()).isEqualTo(EstadoPago.REGISTRADO);
        assertThat(mov.getMontoAplicadoArs()).isEqualByComparingTo("2500000.00");
    }

    @Test
    void anulacionGeneraReverso() {
        Pago pago = pagoBase(102L, "800000.00", "800000.00", "admin");
        when(movimientoCajaRepository.existsByPagoIdAndTipoMovimiento(102L, TipoMovimientoCaja.REVERSO)).thenReturn(false);
        when(movimientoCajaRepository.save(any(MovimientoCaja.class))).thenAnswer(inv -> inv.getArgument(0));

        movimientoCajaService.registrarDesdePago(pago, TipoMovimientoCaja.REVERSO, EstadoPago.ANULADO, true);

        ArgumentCaptor<MovimientoCaja> captor = ArgumentCaptor.forClass(MovimientoCaja.class);
        verify(movimientoCajaRepository).save(captor.capture());
        MovimientoCaja mov = captor.getValue();
        assertThat(mov.getTipoMovimiento()).isEqualTo(TipoMovimientoCaja.REVERSO);
        assertThat(mov.getEstado()).isEqualTo(EstadoPago.ANULADO);
        assertThat(mov.getMontoAplicadoArs()).isEqualByComparingTo("800000.00");
    }

    @Test
    void entregaUsadoGeneraInformativoSinImpactoMonetario() {
        Pago pago = pagoBase(103L, "12000000.00", "12000000.00", "admin");
        when(movimientoCajaRepository.existsByPagoIdAndTipoMovimiento(103L, TipoMovimientoCaja.INFORMATIVO)).thenReturn(false);
        when(movimientoCajaRepository.save(any(MovimientoCaja.class))).thenAnswer(inv -> inv.getArgument(0));

        movimientoCajaService.registrarDesdePago(pago, TipoMovimientoCaja.INFORMATIVO, EstadoPago.REGISTRADO, false);

        ArgumentCaptor<MovimientoCaja> captor = ArgumentCaptor.forClass(MovimientoCaja.class);
        verify(movimientoCajaRepository).save(captor.capture());
        assertThat(captor.getValue().getMontoAplicadoArs()).isEqualByComparingTo("0");
    }

    @Test
    void noPermiteBorrarMovimientos() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> movimientoCajaService.delete(99L));
        assertThat(ex.getMessage()).contains("No se permite borrar movimientos de caja");
    }

    @Test
    void resumenDiarioCalculaIngresosReversosYNeto() {
        MetodoPago transferencia = new MetodoPago();
        transferencia.setId(3L);
        transferencia.setCodigo("TRANSFERENCIA");
        transferencia.setDescripcion("Transferencia");

        MovimientoCaja ingreso = movimiento(TipoMovimientoCaja.INGRESO, "1000.00", transferencia);
        MovimientoCaja reverso = movimiento(TipoMovimientoCaja.REVERSO, "250.00", transferencia);
        MovimientoCaja informativo = movimiento(TipoMovimientoCaja.INFORMATIVO, "0.00", transferencia);

        when(
            movimientoCajaRepository.findAllByFiltrosUsuario(
                any(),
                any(),
                any(),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)
            )
        )
            .thenReturn(new PageImpl<>(List.of(ingreso, reverso, informativo)));

        ResumenDiarioCajaDTO resumen = movimientoCajaService.obtenerResumenDiario(LocalDate.now());

        assertThat(resumen.getTotalIngresos()).isEqualByComparingTo("1000.00");
        assertThat(resumen.getTotalReversos()).isEqualByComparingTo("250.00");
        assertThat(resumen.getNeto()).isEqualByComparingTo("750.00");
        assertThat(resumen.getPorMetodo()).hasSize(1);
        assertThat(resumen.getPorMetodo().get(0).getNeto()).isEqualByComparingTo("750.00");
    }

    @Test
    void noDuplicaMovimientoParaMismoPagoYTipo() {
        Pago pago = pagoBase(104L, "500000.00", "500000.00", "admin");
        when(movimientoCajaRepository.existsByPagoIdAndTipoMovimiento(104L, TipoMovimientoCaja.INGRESO)).thenReturn(true);

        movimientoCajaService.registrarDesdePago(pago, TipoMovimientoCaja.INGRESO, EstadoPago.REGISTRADO, true);

        verify(movimientoCajaRepository, never()).save(any(MovimientoCaja.class));
    }

    private Pago pagoBase(Long id, String monto, String montoAplicadoVenta, String usuarioRegistro) {
        Pago pago = new Pago();
        pago.setId(id);
        pago.setMonto(new BigDecimal(monto));
        pago.setMontoAplicadoVenta(new BigDecimal(montoAplicadoVenta));
        pago.setFecha(Instant.now());
        pago.setUsuarioRegistro(usuarioRegistro);
        return pago;
    }

    private MovimientoCaja movimiento(TipoMovimientoCaja tipo, String montoAplicadoArs, MetodoPago metodoPago) {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setMontoAplicadoArs(new BigDecimal(montoAplicadoArs));
        movimiento.setMetodoPago(metodoPago);
        movimiento.setFecha(Instant.now());
        movimiento.setEstado(EstadoPago.REGISTRADO);
        return movimiento;
    }
}

