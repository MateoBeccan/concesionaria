package com.concesionaria.app.service.impl;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.repository.TipoComprobanteRepository;
import com.concesionaria.app.service.ComprobanteService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagoComprobanteBridgeTest {

    @Mock
    private ComprobanteService comprobanteService;
    @Mock
    private TipoComprobanteRepository tipoComprobanteRepository;
    @Mock
    private ComprobanteRepository comprobanteRepository;

    @Test
    void emiteComprobanteCuandoCorresponde() {
        PagoTextNormalizer normalizer = new PagoTextNormalizer();
        PagoMetodoPolicy policy = new PagoMetodoPolicy(null, normalizer);
        PagoComprobanteBridge bridge = new PagoComprobanteBridge(comprobanteService, tipoComprobanteRepository, comprobanteRepository, policy);
        Pago pago = pagoConVenta(10L, "CONTADO");
        TipoComprobante tipo = new TipoComprobante();
        tipo.setId(2L);
        tipo.setCodigo("REC");
        when(tipoComprobanteRepository.findByCodigoIgnoreCase("REC")).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsByPagoIdAndTipoComprobanteIdAndEstado(10L, 2L, EstadoComprobante.EMITIDO)).thenReturn(false);

        bridge.emitirSiCorresponde(pago, pago.getMetodoPago());

        verify(comprobanteService).emitirComprobantePago(10L, 2L);
    }

    @Test
    void noEmiteDuplicado() {
        PagoTextNormalizer normalizer = new PagoTextNormalizer();
        PagoMetodoPolicy policy = new PagoMetodoPolicy(null, normalizer);
        PagoComprobanteBridge bridge = new PagoComprobanteBridge(comprobanteService, tipoComprobanteRepository, comprobanteRepository, policy);
        Pago pago = pagoConVenta(11L, "CONTADO");
        TipoComprobante tipo = new TipoComprobante();
        tipo.setId(3L);
        tipo.setCodigo("REC");
        when(tipoComprobanteRepository.findByCodigoIgnoreCase("REC")).thenReturn(Optional.of(tipo));
        when(comprobanteRepository.existsByPagoIdAndTipoComprobanteIdAndEstado(11L, 3L, EstadoComprobante.EMITIDO)).thenReturn(true);

        bridge.emitirSiCorresponde(pago, pago.getMetodoPago());

        verify(comprobanteService, never()).emitirComprobantePago(anyLong(), anyLong());
    }

    @Test
    void anulaComprobantesAsociados() {
        PagoTextNormalizer normalizer = new PagoTextNormalizer();
        PagoMetodoPolicy policy = new PagoMetodoPolicy(null, normalizer);
        PagoComprobanteBridge bridge = new PagoComprobanteBridge(comprobanteService, tipoComprobanteRepository, comprobanteRepository, policy);
        Pago pago = new Pago();
        pago.setId(12L);
        Comprobante comp = new Comprobante();
        comp.setEstado(EstadoComprobante.EMITIDO);
        when(comprobanteRepository.findAllByPagoIdOrderByFechaEmisionDescIdDesc(12L)).thenReturn(List.of(comp));

        bridge.anularComprobantesAsociados(pago, "Motivo", "admin", Instant.now());

        verify(comprobanteRepository).save(comp);
    }

    private Pago pagoConVenta(Long pagoId, String metodoCodigo) {
        Pago pago = new Pago();
        pago.setId(pagoId);
        MetodoPago metodo = new MetodoPago();
        metodo.setCodigo(metodoCodigo);
        pago.setMetodoPago(metodo);
        Venta venta = new Venta();
        venta.setId(200L);
        pago.setVenta(venta);
        return pago;
    }
}
