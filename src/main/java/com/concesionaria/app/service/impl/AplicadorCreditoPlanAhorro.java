package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.AdjudicacionPlanAhorro;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.PagoService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.MetodoPagoDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AplicadorCreditoPlanAhorro {

    private static final String CODIGO_METODO_PLAN_AHORRO = "PLAN_AHORRO";
    private static final String OBS_PAGO_PLAN_AHORRO = "Aplicacion automatica de cuotas de plan de ahorro";

    private final PagoService pagoService;
    private final PagoRepository pagoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final VentaRepository ventaRepository;
    private final VentaService ventaService;

    public AplicadorCreditoPlanAhorro(
        PagoService pagoService,
        PagoRepository pagoRepository,
        MetodoPagoRepository metodoPagoRepository,
        VentaRepository ventaRepository,
        VentaService ventaService
    ) {
        this.pagoService = pagoService;
        this.pagoRepository = pagoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.ventaRepository = ventaRepository;
        this.ventaService = ventaService;
    }

    public void aplicarCreditoSiCorresponde(AdjudicacionPlanAhorro adjudicacion, VentaDTO ventaCreada) {
        if (ventaCreada == null || ventaCreada.getId() == null || adjudicacion == null || adjudicacion.getId() == null) {
            return;
        }
        BigDecimal montoReconocido = adjudicacion.getMontoReconocidoCuotas() == null
            ? BigDecimal.ZERO
            : adjudicacion.getMontoReconocidoCuotas().setScale(2, RoundingMode.HALF_UP);
        if (montoReconocido.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal totalVenta = ventaCreada.getTotal() == null ? BigDecimal.ZERO : ventaCreada.getTotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoAplicar = montoReconocido.min(totalVenta);
        if (montoAplicar.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (pagoRepository.existsByVentaIdAndMetodoPagoCodigoAndEstado(ventaCreada.getId(), CODIGO_METODO_PLAN_AHORRO, EstadoPago.REGISTRADO)) {
            return;
        }

        MetodoPagoDTO metodoPagoDTO = new MetodoPagoDTO();
        MetodoPago metodoPago = metodoPagoRepository
            .findByCodigoIgnoreCase(CODIGO_METODO_PLAN_AHORRO)
            .orElseThrow(() -> new BadRequestException("No existe metodo de pago PLAN_AHORRO configurado"));
        metodoPagoDTO.setId(metodoPago.getId());

        VentaDTO ventaRef = new VentaDTO();
        ventaRef.setId(ventaCreada.getId());

        MonedaDTO monedaDTO = new MonedaDTO();
        monedaDTO.setId(ventaCreada.getMoneda().getId());

        PagoDTO pagoDTO = new PagoDTO();
        pagoDTO.setVenta(ventaRef);
        pagoDTO.setMetodoPago(metodoPagoDTO);
        pagoDTO.setMoneda(monedaDTO);
        pagoDTO.setMonto(montoAplicar);
        pagoDTO.setFecha(Instant.now());
        pagoDTO.setObservaciones(OBS_PAGO_PLAN_AHORRO);
        pagoDTO.setAdjudicacionPlanAhorroId(adjudicacion.getId());
        pagoDTO.setContratoPlanAhorroId(adjudicacion.getContratoPlanAhorro().getId());

        pagoService.registrarPago(ventaCreada.getId(), pagoDTO);
    }

    public void ajustarEstadoFinalVentaDesdePlan(Long ventaId, String usuario) {
        if (ventaId == null) {
            return;
        }
        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta generada no existe"));
        BigDecimal total = venta.getTotal() == null ? BigDecimal.ZERO : venta.getTotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPagado = pagoRepository.sumMontoByVentaId(ventaId);
        totalPagado = totalPagado == null ? BigDecimal.ZERO : totalPagado.setScale(2, RoundingMode.HALF_UP);
        if (totalPagado.compareTo(total) > 0) {
            totalPagado = total;
        }
        BigDecimal saldo = total.subtract(totalPagado).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        venta.setTotalPagado(totalPagado);
        venta.setSaldo(saldo);
        venta.setEstado(saldo.compareTo(BigDecimal.ZERO) == 0 ? EstadoVenta.PAGADA : EstadoVenta.PENDIENTE);
        venta.setLastModifiedDate(Instant.now());
        venta.setLastModifiedBy(usuario);
        ventaRepository.save(venta);
        if (venta.getEstado() == EstadoVenta.PAGADA) {
            ventaService.sincronizarInventarioConVenta(venta.getId());
        }
    }
}

