package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.repository.ComprobantePlanAhorroRepository;
import com.concesionaria.app.service.dto.ContratoPlanAhorroDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContratoPlanAhorroValidator {

    private final ComprobantePlanAhorroRepository comprobantePlanAhorroRepository;

    public ContratoPlanAhorroValidator(ComprobantePlanAhorroRepository comprobantePlanAhorroRepository) {
        this.comprobantePlanAhorroRepository = comprobantePlanAhorroRepository;
    }

    public void validarContratoNuevo(ContratoPlanAhorroDTO dto) {
        if (dto.getPlan() == null || dto.getPlan().getId() == null) {
            throw new BadRequestException("Debe seleccionar un plan");
        }
        if (dto.getCliente() == null || dto.getCliente().getId() == null) {
            throw new BadRequestException("Debe seleccionar un cliente");
        }
    }

    public void validarMontoPagoIndividual(BigDecimal monto, BigDecimal importeCuota) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto de pago debe ser mayor a cero");
        }
        if (monto.compareTo(importeCuota) != 0) {
            throw new BadRequestException("El monto debe coincidir con el importe de la cuota para esta fase");
        }
    }

    public void validarCuotaPagable(CuotaPlanAhorro cuota) {
        if (cuota.getEstado() == EstadoCuotaPlanAhorro.PAGADA) {
            throw new BadRequestException("La cuota ya fue pagada");
        }
        if (cuota.getEstado() == EstadoCuotaPlanAhorro.ANULADA) {
            throw new BadRequestException("La cuota fue anulada");
        }
    }

    public void validarSeleccionCuotas(List<CuotaPlanAhorro> cuotas) {
        if (cuotas == null || cuotas.isEmpty()) {
            throw new BadRequestException("Debe seleccionar al menos una cuota");
        }
        Long contratoId = cuotas.getFirst().getContrato().getId();
        boolean distintoContrato = cuotas.stream().anyMatch(cuota -> !cuota.getContrato().getId().equals(contratoId));
        if (distintoContrato) {
            throw new BadRequestException("Todas las cuotas deben pertenecer al mismo contrato");
        }
    }

    public void validarCuotaPagableEnLote(CuotaPlanAhorro cuota) {
        if (cuota.getEstado() != EstadoCuotaPlanAhorro.PENDIENTE && cuota.getEstado() != EstadoCuotaPlanAhorro.VENCIDA) {
            throw new BadRequestException("Solo se pueden pagar cuotas pendientes o vencidas");
        }
        if (cuota.getPago() != null) {
            throw new BadRequestException("Una o mÃ¡s cuotas ya tienen un pago asociado");
        }
        if (comprobantePlanAhorroRepository.existsByCuotaPlanAhorroIdAndEstado(cuota.getId(), EstadoComprobante.EMITIDO)) {
            throw new BadRequestException("Una o mÃ¡s cuotas ya tienen un comprobante activo");
        }
    }

    public void validarMontoTotalEsperado(BigDecimal montoTotal, BigDecimal totalEsperado) {
        if (montoTotal != null && montoTotal.setScale(2, RoundingMode.HALF_UP).compareTo(totalEsperado) != 0) {
            throw new BadRequestException("El monto total debe coincidir con la suma de cuotas seleccionadas");
        }
    }
}

