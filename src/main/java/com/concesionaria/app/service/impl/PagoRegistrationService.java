package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.repository.PagoRepository;
import org.springframework.stereotype.Service;

@Service
public class PagoRegistrationService {

    private final PagoRepository pagoRepository;
    private final PagoTextNormalizer pagoTextNormalizer;

    public PagoRegistrationService(PagoRepository pagoRepository, PagoTextNormalizer pagoTextNormalizer) {
        this.pagoRepository = pagoRepository;
        this.pagoTextNormalizer = pagoTextNormalizer;
    }

    public Pago registrar(RegistrarPagoCommand command) {
        Pago pago = new Pago();
        pago.setFecha(command.fecha());
        pago.setMonto(command.monto());
        pago.setMoneda(command.moneda());
        pago.setMetodoPago(command.metodoPago());
        pago.setEntidadFinanciera(command.entidadFinanciera());
        pago.setTipoMovimiento(command.tipoMovimiento());
        pago.setEstado(command.estado());
        pago.setCotizacionUsada(command.cotizacionUsada());
        pago.setMontoAplicadoVenta(command.montoAplicadoVenta());
        pago.setFechaCotizacionUsada(command.fechaCotizacionUsada());
        pago.setCotizacionRef(command.cotizacionRef());
        pago.setVenta(command.venta());
        pago.setReserva(command.reserva());
        pago.setTasacionUsado(command.tasacionUsado());
        pago.setAdjudicacionPlanAhorro(command.adjudicacionPlanAhorro());
        pago.setContratoPlanAhorro(command.contratoPlanAhorro());
        pago.setUsuarioRegistro(normalizarTexto(command.usuarioRegistro(), 50));
        pago.setReferencia(normalizarTexto(command.referencia(), 100));
        pago.setNumeroOperacion(normalizarTexto(command.numeroOperacion(), 100));
        pago.setComprobanteExterno(normalizarTexto(command.comprobanteExterno(), 100));
        pago.setBancoEntidad(normalizarTexto(command.bancoEntidad(), 100));
        pago.setObservaciones(normalizarTexto(command.observaciones(), 500));
        pago.setMotivoAnulacion(normalizarTexto(command.motivoAnulacion(), 500));
        pago.setUsuarioAnulacion(normalizarTexto(command.usuarioAnulacion(), 50));
        pago.setFechaAnulacion(command.fechaAnulacion());
        pago.setCreatedDate(command.createdDate());
        pago.setLastModifiedDate(command.lastModifiedDate());
        return pagoRepository.save(pago);
    }

    private String normalizarTexto(String value, int max) {
        return pagoTextNormalizer.normalizarTexto(value, max);
    }
}
