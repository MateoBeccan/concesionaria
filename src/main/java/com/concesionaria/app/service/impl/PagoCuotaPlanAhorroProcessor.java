package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.service.ComprobantePlanAhorroService;
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

    private final ContratoPlanAhorroRepository contratoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PagoRegistrationService pagoRegistrationService;
    private final CuotaPlanAhorroRepository cuotaRepository;
    private final PagoCajaBridge pagoCajaBridge;
    private final ComprobantePlanAhorroService comprobantePlanAhorroService;
    private final ContratoPlanAhorroValidator validator;
    private final ContratoPlanAhorroCalculator calculator;
    private final CuotaPlanAhorroMapper cuotaMapper;

    public PagoCuotaPlanAhorroProcessor(
        ContratoPlanAhorroRepository contratoRepository,
        MetodoPagoRepository metodoPagoRepository,
        PagoRegistrationService pagoRegistrationService,
        CuotaPlanAhorroRepository cuotaRepository,
        PagoCajaBridge pagoCajaBridge,
        ComprobantePlanAhorroService comprobantePlanAhorroService,
        ContratoPlanAhorroValidator validator,
        ContratoPlanAhorroCalculator calculator,
        CuotaPlanAhorroMapper cuotaMapper
    ) {
        this.contratoRepository = contratoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.pagoRegistrationService = pagoRegistrationService;
        this.cuotaRepository = cuotaRepository;
        this.pagoCajaBridge = pagoCajaBridge;
        this.comprobantePlanAhorroService = comprobantePlanAhorroService;
        this.validator = validator;
        this.calculator = calculator;
        this.cuotaMapper = cuotaMapper;
    }

    public CuotaPlanAhorroDTO pagarCuota(CuotaPlanAhorro cuota, BigDecimal monto, String observaciones, String login) {
        validator.validarCuotaPagable(cuota);
        validator.validarMontoPagoIndividual(monto, cuota.getImporte());

        ContratoPlanAhorro contrato = bloquearContrato(cuota.getContrato());
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
        pagoCajaBridge.registrarPago(pago);
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

        ContratoPlanAhorro contrato = bloquearContrato(cuotas.getFirst().getContrato());
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
        pagoCajaBridge.registrarPago(pago);
        return cuotas.stream().map(cuotaMapper::toDto).toList();
    }

    private ContratoPlanAhorro bloquearContrato(ContratoPlanAhorro contrato) {
        if (contrato == null || contrato.getId() == null) {
            throw new BadRequestException("Contrato inexistente");
        }
        return contratoRepository.findByIdForUpdate(contrato.getId()).orElseThrow(() -> new BadRequestException("Contrato inexistente"));
    }
    private Pago crearPagoContrato(
        ContratoPlanAhorro contrato,
        BigDecimal monto,
        MetodoPago metodoPago,
        String observaciones,
        String login,
        Instant ahora
    ) {
        return pagoRegistrationService.registrar(
            new RegistrarPagoCommand(
                ahora,
                monto,
                contrato.getPlan().getMoneda(),
                metodoPago,
                null,
                TipoMovimientoPago.PAGO_RECIBIDO,
                EstadoPago.REGISTRADO,
                BigDecimal.ONE,
                monto,
                ahora,
                null,
                null,
                null,
                null,
                null,
                contrato,
                login,
                null,
                null,
                null,
                null,
                observaciones,
                null,
                null,
                null,
                ahora,
                ahora
            )
        );
    }

    private MetodoPago resolverMetodoPago(Long metodoPagoId) {
        if (metodoPagoId != null) {
            return metodoPagoRepository.findById(metodoPagoId).orElseThrow(() -> new BadRequestException("El mÃ©todo de pago no existe"));
        }
        return metodoPagoRepository
            .findByCodigoIgnoreCase(MetodoPagoPolicy.CODIGO_CONTADO)
            .orElseThrow(() -> new BadRequestException("No existe el metodo de pago CONTADO configurado"));
    }
}
