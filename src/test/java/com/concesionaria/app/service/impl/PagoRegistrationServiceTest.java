package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.Cotizacion;
import com.concesionaria.app.domain.EntidadFinanciera;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.repository.PagoRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagoRegistrationServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Test
    void registrarConstruyeNormalizaYPersistePagoDeVenta() {
        PagoRegistrationService service = new PagoRegistrationService(pagoRepository, new PagoTextNormalizer());
        Instant fecha = Instant.parse("2026-09-25T12:00:00Z");
        Venta venta = new Venta();
        venta.setId(10L);
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId(20L);
        Moneda moneda = new Moneda();
        moneda.setId(30L);
        EntidadFinanciera entidadFinanciera = new EntidadFinanciera();
        entidadFinanciera.setId(40L);
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setId(50L);
        TasacionUsado tasacion = new TasacionUsado();
        tasacion.setId(60L);

        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago pago = inv.getArgument(0);
            pago.setId(9001L);
            return pago;
        });

        Pago result = service.registrar(
            new RegistrarPagoCommand(
                fecha,
                new BigDecimal("100.50"),
                moneda,
                metodoPago,
                entidadFinanciera,
                TipoMovimientoPago.ENTREGA_USADO,
                EstadoPago.REGISTRADO,
                new BigDecimal("1.00000000"),
                new BigDecimal("100.50"),
                fecha,
                cotizacion,
                venta,
                null,
                tasacion,
                null,
                null,
                " asesor ",
                " ref-123 ",
                " op-123 ",
                " comp-123 ",
                " banco ",
                " observacion ",
                null,
                null,
                null,
                fecha,
                fecha
            )
        );

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        Pago pago = pagoCaptor.getValue();
        assertThat(result.getId()).isEqualTo(9001L);
        assertThat(pago.getVenta()).isSameAs(venta);
        assertThat(pago.getReserva()).isNull();
        assertThat(pago.getTasacionUsado()).isSameAs(tasacion);
        assertThat(pago.getMetodoPago()).isSameAs(metodoPago);
        assertThat(pago.getMoneda()).isSameAs(moneda);
        assertThat(pago.getEntidadFinanciera()).isSameAs(entidadFinanciera);
        assertThat(pago.getCotizacionRef()).isSameAs(cotizacion);
        assertThat(pago.getMonto()).isEqualByComparingTo("100.50");
        assertThat(pago.getMontoAplicadoVenta()).isEqualByComparingTo("100.50");
        assertThat(pago.getCotizacionUsada()).isEqualByComparingTo("1.00000000");
        assertThat(pago.getTipoMovimiento()).isEqualTo(TipoMovimientoPago.ENTREGA_USADO);
        assertThat(pago.getEstado()).isEqualTo(EstadoPago.REGISTRADO);
        assertThat(pago.getFecha()).isEqualTo(fecha);
        assertThat(pago.getFechaCotizacionUsada()).isEqualTo(fecha);
        assertThat(pago.getCreatedDate()).isEqualTo(fecha);
        assertThat(pago.getLastModifiedDate()).isEqualTo(fecha);
        assertThat(pago.getUsuarioRegistro()).isEqualTo("asesor");
        assertThat(pago.getReferencia()).isEqualTo("ref-123");
        assertThat(pago.getNumeroOperacion()).isEqualTo("op-123");
        assertThat(pago.getComprobanteExterno()).isEqualTo("comp-123");
        assertThat(pago.getBancoEntidad()).isEqualTo("banco");
        assertThat(pago.getObservaciones()).isEqualTo("observacion");
    }

    @Test
    void registrarPermiteReservaYContratoCuandoElCommandLosIncluye() {
        PagoRegistrationService service = new PagoRegistrationService(pagoRepository, new PagoTextNormalizer());
        Instant fecha = Instant.parse("2026-09-25T13:00:00Z");
        Reserva reserva = new Reserva();
        reserva.setId(70L);
        ContratoPlanAhorro contrato = new ContratoPlanAhorro();
        contrato.setId(80L);

        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrar(
            new RegistrarPagoCommand(
                fecha,
                new BigDecimal("200.00"),
                null,
                null,
                null,
                TipoMovimientoPago.ANTICIPO,
                EstadoPago.REGISTRADO,
                BigDecimal.ONE,
                new BigDecimal("200.00"),
                fecha,
                null,
                null,
                reserva,
                null,
                null,
                contrato,
                "asesor",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                fecha,
                fecha
            )
        );

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        Pago pago = pagoCaptor.getValue();
        assertThat(pago.getVenta()).isNull();
        assertThat(pago.getReserva()).isSameAs(reserva);
        assertThat(pago.getContratoPlanAhorro()).isSameAs(contrato);
        assertThat(pago.getTipoMovimiento()).isEqualTo(TipoMovimientoPago.ANTICIPO);
    }
}
