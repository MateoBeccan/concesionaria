package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.service.ComprobantePlanAhorroService;
import com.concesionaria.app.service.MovimientoCajaService;
import com.concesionaria.app.service.dto.CuotaPlanAhorroDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.CuotaPlanAhorroMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PagoCuotaPlanAhorroProcessor {

    private static final String CODIGO_CONTADO = "CONTADO";

    private final MetodoPagoRepository metodoPagoRepository;
    private final PagoRepository pagoRepository;
    private final CuotaPlanAhorroRepository cuotaRepository;
    private final MovimientoCajaService movimientoCajaService;
    private final ComprobantePlanAhorroService comprobantePlanAhorroService;
    private final ContratoPlanAhorroValidator validator;
    private final ContratoPlanAhorroCalculator calculator;
    private final CuotaPlanAhorroMapper cuotaMapper;

    public PagoCuotaPlanAhorroProcessor(
        MetodoPagoRepository metodoPagoRepository,
        PagoRepository pagoRepository,
        CuotaPlanAhorroRepository cuotaRepository,
        MovimientoCajaService movimientoCajaService,
        ComprobantePlanAhorroService comprobantePlanAhorroService,
        ContratoPlanAhorroValidator validator,
        ContratoPlanAhorroCalculator calculator,
        CuotaPlanAhorroMapper cuotaMapper
    ) {
        this.metodoPagoRepository = metodoPagoRepository;
        this.pagoRepository = pagoRepository;
        this.cuotaRepository = cuotaRepository;
        this.movimientoCajaService = movimientoCajaService;
        this.comprobantePlanAhorroService = comprobantePlanAhorroService;
        this.validator = validator;
        this.calculator = calculator;
        this.cuotaMapper = cuotaMapper;
    }

    public CuotaPlanAhorroDTO pagarCuota(CuotaPlanAhorro cuota, BigDecimal monto, String observaciones, String login) {
        validator.validarCuotaPagable(cuota);
        validator.validarMontoPagoIndividual(monto, cuota.getImporte());

        ContratoPlanAhorro contrato = cuota.getContrato();
        Instant ahora = Instant.now();
        Pago pago = crearPagoContrato(
            contrato,
            monto.setScale(2, RoundingMode.HALF_UP),
            resolverMetodoPago(null),
            observaciones,
            login,
            ahora
        );

        cuota.setPago(pago);
        cuota.setEstado(com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro.PAGADA);
        cuota.setFechaPago(ahora);
        cuotaRepository.save(cuota);

        calculator.recalcularContrato(contrato);
        movimientoCajaService.registrarDesdePago(pago, TipoMovimientoCaja.INGRESO, EstadoPago.REGISTRADO, true);
        comprobantePlanAhorroService.emitirParaCuota(cuota, pago);

        return cuotaMapper.toDto(cuota);
    }

    public List<CuotaPlanAhorroDTO> pagarCuotas(
        List<CuotaPlanAhorro> cuotasInput,
        BigDecimal montoTotal,
        String observaciones,
        Long metodoPagoId,
        Long monedaId,
        String login
    ) {
        validator.validarSeleccionCuotas(cuotasInput);

        List<CuotaPlanAhorro> cuotas = cuotasInput.stream().sorted(Comparator.comparing(CuotaPlanAhorro::getNumeroCuota)).toList();
        cuotas.forEach(validator::validarCuotaPagableEnLote);

        ContratoPlanAhorro contrato = cuotas.getFirst().getContrato();
        BigDecimal totalEsperado = cuotas
            .stream()
            .map(CuotaPlanAhorro::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        validator.validarMontoTotalEsperado(montoTotal, totalEsperado);

        if (monedaId != null && (contrato.getPlan().getMoneda() == null || !monedaId.equals(contrato.getPlan().getMoneda().getId()))) {
            throw new BadRequestException("La moneda del pago debe coincidir con la moneda del plan");
        }

        Instant ahora = Instant.now();
        Pago pago = crearPagoContrato(contrato, totalEsperado, resolverMetodoPago(metodoPagoId), observaciones, login, ahora);

        for (CuotaPlanAhorro cuota : cuotas) {
            cuota.setPago(pago);
            cuota.setEstado(com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro.PAGADA);
            cuota.setFechaPago(ahora);
            cuotaRepository.save(cuota);
            comprobantePlanAhorroService.emitirParaCuota(cuota, pago);
        }

        calculator.recalcularContrato(contrato);
        movimientoCajaService.registrarDesdePago(pago, TipoMovimientoCaja.INGRESO, EstadoPago.REGISTRADO, true);
        return cuotas.stream().map(cuotaMapper::toDto).toList();
    }

    private Pago crearPagoContrato(
        ContratoPlanAhorro contrato,
        BigDecimal monto,
        MetodoPago metodoPago,
        String observaciones,
        String login,
        Instant ahora
    ) {
        Pago pago = new Pago();
        pago.setFecha(ahora);
        pago.setMonto(monto);
        pago.setMoneda(contrato.getPlan().getMoneda());
        pago.setMetodoPago(metodoPago);
        pago.setTipoMovimiento(TipoMovimientoPago.PAGO_RECIBIDO);
        pago.setEstado(EstadoPago.REGISTRADO);
        pago.setCotizacionUsada(BigDecimal.ONE);
        pago.setMontoAplicadoVenta(monto);
        pago.setFechaCotizacionUsada(ahora);
        pago.setVenta(null);
        pago.setReserva(null);
        pago.setTasacionUsado(null);
        pago.setAdjudicacionPlanAhorro(null);
        pago.setContratoPlanAhorro(contrato);
        pago.setUsuarioRegistro(login);
        pago.setObservaciones(observaciones);
        pago.setCreatedDate(ahora);
        pago.setLastModifiedDate(ahora);
        return pagoRepository.save(pago);
    }

    private MetodoPago resolverMetodoPago(Long metodoPagoId) {
        if (metodoPagoId != null) {
            return metodoPagoRepository.findById(metodoPagoId).orElseThrow(() -> new BadRequestException("El mÃ©todo de pago no existe"));
        }
        return metodoPagoRepository
            .findByCodigoIgnoreCase(CODIGO_CONTADO)
            .orElseThrow(() -> new BadRequestException("No existe el metodo de pago CONTADO configurado"));
    }
}
