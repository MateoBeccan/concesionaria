package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.ComprobantePlanAhorroService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.PagoMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PagoAnulacionService {

    private static final Logger LOG = LoggerFactory.getLogger(PagoAnulacionService.class);

    private final PagoRepository pagoRepository;
    private final PagoMapper pagoMapper;
    private final VentaRepository ventaRepository;
    private final ReservaRepository reservaRepository;
    private final VentaService ventaService;
    private final ComprobantePlanAhorroService comprobantePlanAhorroService;
    private final CuotaPlanAhorroRepository cuotaPlanAhorroRepository;
    private final ContratoPlanAhorroRepository contratoPlanAhorroRepository;
    private final PagoTextNormalizer pagoTextNormalizer;
    private final PagoCalculator pagoCalculator;
    private final PagoCajaBridge pagoCajaBridge;
    private final PagoComprobanteBridge pagoComprobanteBridge;

    @Value("${app.negocio.reserva.porcentaje-minimo:0.10}")
    private BigDecimal porcentajeMinimoReserva = new BigDecimal("0.10");

    public PagoAnulacionService(
        PagoRepository pagoRepository,
        PagoMapper pagoMapper,
        VentaRepository ventaRepository,
        ReservaRepository reservaRepository,
        VentaService ventaService,
        ComprobantePlanAhorroService comprobantePlanAhorroService,
        CuotaPlanAhorroRepository cuotaPlanAhorroRepository,
        ContratoPlanAhorroRepository contratoPlanAhorroRepository,
        PagoTextNormalizer pagoTextNormalizer,
        PagoCalculator pagoCalculator,
        PagoCajaBridge pagoCajaBridge,
        PagoComprobanteBridge pagoComprobanteBridge
    ) {
        this.pagoRepository = pagoRepository;
        this.pagoMapper = pagoMapper;
        this.ventaRepository = ventaRepository;
        this.reservaRepository = reservaRepository;
        this.ventaService = ventaService;
        this.comprobantePlanAhorroService = comprobantePlanAhorroService;
        this.cuotaPlanAhorroRepository = cuotaPlanAhorroRepository;
        this.contratoPlanAhorroRepository = contratoPlanAhorroRepository;
        this.pagoTextNormalizer = pagoTextNormalizer;
        this.pagoCalculator = pagoCalculator;
        this.pagoCajaBridge = pagoCajaBridge;
        this.pagoComprobanteBridge = pagoComprobanteBridge;
    }

    public PagoDTO anularPago(Long pagoId, String motivo) {
        Instant ahora = Instant.now();
        Pago pago = pagoRepository.findById(pagoId).orElseThrow(() -> new BadRequestException("El pago no existe"));
        if (pago.getEstado() == EstadoPago.ANULADO) {
            throw new BadRequestException("El pago ya se encuentra anulado");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Debe informar un motivo para anular el pago");
        }
        if (pago.getTipoMovimiento() == TipoMovimientoPago.ENTREGA_USADO && pago.getTasacionUsado() != null && pago.getTasacionUsado().getInventarioGenerado() != null) {
            throw new BadRequestException("No se puede anular la entrega de usado porque ya genero inventario");
        }
        Venta venta = pago.getVenta();
        Reserva reservaAsociada = pago.getReserva();
        CuotaPlanAhorro cuotaPlan = cuotaPlanAhorroRepository == null ? null : cuotaPlanAhorroRepository.findFirstByPagoId(pagoId).orElse(null);
        if ((venta == null || venta.getId() == null) && (reservaAsociada == null || reservaAsociada.getId() == null)) {
            if (cuotaPlan == null) {
                throw new BadRequestException("El pago no tiene una operacion asociada");
            }
        }
        pago.setEstado(EstadoPago.ANULADO);
        if (pago.getTipoMovimiento() == TipoMovimientoPago.PAGO_RECIBIDO || pago.getTipoMovimiento() == TipoMovimientoPago.ANTICIPO) {
            pago.setTipoMovimiento(TipoMovimientoPago.ANULACION);
        }
        if (pago.getTipoMovimiento() == TipoMovimientoPago.ENTREGA_USADO && venta != null && venta.getTasacionUsado() != null) {
            venta.setTasacionUsado(null);
            venta.setLastModifiedDate(ahora);
            ventaRepository.save(venta);
        }
        String usuarioAnulacion = currentUserLogin();
        Instant fechaAnulacion = ahora;
        pago.setMotivoAnulacion(motivo.trim());
        pago.setUsuarioAnulacion(usuarioAnulacion);
        pago.setFechaAnulacion(fechaAnulacion);
        pago.setLastModifiedDate(ahora);
        normalizarCamposTextoPago(pago);
        LOG.info("Anulando pago pagoId={} ventaId={} reservaId={}", pagoId, venta != null ? venta.getId() : null, reservaAsociada != null ? reservaAsociada.getId() : null);
        Pago pagoActualizado = pagoRepository.save(pago);
        pagoCajaBridge.registrarAnulacion(pagoActualizado);
        pagoComprobanteBridge.anularComprobantesAsociados(pagoActualizado, motivo, usuarioAnulacion, fechaAnulacion);
        comprobantePlanAhorroService.anularPorPago(pagoActualizado.getId(), motivo, usuarioAnulacion, fechaAnulacion);

        if (cuotaPlan != null && cuotaPlanAhorroRepository != null) {
            cuotaPlan.setEstado(EstadoCuotaPlanAhorro.PENDIENTE);
            cuotaPlan.setFechaPago(null);
            cuotaPlan.setPago(null);
            cuotaPlanAhorroRepository.save(cuotaPlan);
            recalcContratoPlan(cuotaPlan.getContrato());
        }

        if (venta != null && venta.getId() != null) {
            recalcularVentaEInventario(venta);
        } else if (reservaAsociada != null && reservaAsociada.getId() != null) {
            Reserva reserva = reservaRepository.findById(reservaAsociada.getId()).orElse(null);
            if (reserva != null) {
                BigDecimal total = pagoRepository.sumMontoByReservaId(reserva.getId());
                reserva.setMontoSenia(total == null ? pagoTextNormalizer.normalizarMoneda(BigDecimal.ZERO) : pagoTextNormalizer.normalizarMoneda(total));
                reserva.setLastModifiedDate(ahora);
                reservaRepository.save(reserva);
            }
        }
        return pagoMapper.toDto(pagoActualizado);
    }

    private void recalcularVentaEInventario(Venta venta) {
        Instant ahora = Instant.now();
        BigDecimal totalPagado = totalPagadoRegistrado(venta.getId());
        BigDecimal saldo = pagoCalculator.calcularSaldoVenta(venta, totalPagado);
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Los pagos registrados superan el total de la venta");
        }

        venta.setTotalPagado(totalPagado);
        venta.setSaldo(saldo);
        venta.setLastModifiedDate(ahora);
        ventaRepository.save(venta);

        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            ventaService.confirmarVenta(venta.getId());
        } else {
            BigDecimal minimoReserva = pagoCalculator.calcularMontoMinimoReserva(venta, porcentajeMinimoReserva);
            if (totalPagado.compareTo(minimoReserva) >= 0 && minimoReserva.compareTo(BigDecimal.ZERO) > 0) {
                venta.setEstado(EstadoVenta.RESERVADA);
            } else {
                venta.setEstado(EstadoVenta.PENDIENTE);
            }
            venta.setLastModifiedDate(ahora);
            ventaRepository.save(venta);
            ventaService.sincronizarInventarioConVenta(venta.getId());
        }
    }

    private BigDecimal totalPagadoRegistrado(Long ventaId) {
        BigDecimal total = pagoRepository.sumMontoByVentaId(ventaId);
        if (total == null) {
            return pagoTextNormalizer.normalizarMoneda(BigDecimal.ZERO);
        }
        return pagoTextNormalizer.normalizarMoneda(total);
    }

    private void recalcContratoPlan(ContratoPlanAhorro contrato) {
        if (contrato == null || contrato.getId() == null || cuotaPlanAhorroRepository == null || contratoPlanAhorroRepository == null) {
            return;
        }
        List<CuotaPlanAhorro> cuotas = cuotaPlanAhorroRepository.findAllByContratoIdOrderByNumeroCuotaAsc(contrato.getId());
        long pagadas = cuotas.stream().filter(c -> c.getEstado() == EstadoCuotaPlanAhorro.PAGADA).count();
        BigDecimal saldo = cuotas
            .stream()
            .filter(c -> c.getEstado() == EstadoCuotaPlanAhorro.PENDIENTE || c.getEstado() == EstadoCuotaPlanAhorro.VENCIDA)
            .map(CuotaPlanAhorro::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, PagoTextNormalizer.REDONDEO);
        contrato.setCuotasPagadas((int) pagadas);
        contrato.setSaldoPendiente(saldo);
        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            contrato.setEstado(EstadoContratoPlanAhorro.FINALIZADO);
        } else if (contrato.getEstado() == EstadoContratoPlanAhorro.FINALIZADO) {
            contrato.setEstado(EstadoContratoPlanAhorro.ACTIVO);
        }
        contratoPlanAhorroRepository.save(contrato);
    }

    private void normalizarCamposTextoPago(Pago pago) {
        pago.setReferencia(pagoTextNormalizer.normalizarTexto(pago.getReferencia(), 100));
        pago.setNumeroOperacion(pagoTextNormalizer.normalizarTexto(pago.getNumeroOperacion(), 100));
        pago.setComprobanteExterno(pagoTextNormalizer.normalizarTexto(pago.getComprobanteExterno(), 100));
        pago.setBancoEntidad(pagoTextNormalizer.normalizarTexto(pago.getBancoEntidad(), 100));
        pago.setObservaciones(pagoTextNormalizer.normalizarTexto(pago.getObservaciones(), 500));
        pago.setUsuarioRegistro(pagoTextNormalizer.normalizarTexto(pago.getUsuarioRegistro(), 50));
        pago.setMotivoAnulacion(pagoTextNormalizer.normalizarTexto(pago.getMotivoAnulacion(), 500));
        pago.setUsuarioAnulacion(pagoTextNormalizer.normalizarTexto(pago.getUsuarioAnulacion(), 50));
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }
}
