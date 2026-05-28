package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.ReglaAdjudicacionPlan;
import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.TipoReglaAdjudicacionPlan;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.service.dto.ElegibilidadAdjudicacionDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class ElegibilidadAdjudicacionEvaluator {

    private final CuotaPlanAhorroRepository cuotaRepository;

    public ElegibilidadAdjudicacionEvaluator(CuotaPlanAhorroRepository cuotaRepository) {
        this.cuotaRepository = cuotaRepository;
    }

    public ElegibilidadAdjudicacionDTO evaluar(ContratoPlanAhorro contrato) {
        ElegibilidadAdjudicacionDTO dto = new ElegibilidadAdjudicacionDTO();
        ReglaAdjudicacionPlan regla = contrato.getPlan() != null ? contrato.getPlan().getReglaAdjudicacion() : null;
        if (regla == null) {
            regla = reglaSinReglaDefault();
        }
        TipoReglaAdjudicacionPlan tipo = regla.getTipoRegla() == null ? TipoReglaAdjudicacionPlan.SIN_REGLA : regla.getTipoRegla();

        int cuotasPagadas = contrato.getCuotasPagadas() == null ? 0 : contrato.getCuotasPagadas();
        BigDecimal valorMovil = contrato.getPlan() == null || contrato.getPlan().getValorMovil() == null
            ? BigDecimal.ZERO
            : contrato.getPlan().getValorMovil().setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldoPendiente = contrato.getSaldoPendiente() == null ? BigDecimal.ZERO : contrato.getSaldoPendiente().setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoPagado = valorMovil.subtract(saldoPendiente).max(BigDecimal.ZERO);
        BigDecimal porcentajeIntegrado = valorMovil.compareTo(BigDecimal.ZERO) > 0
            ? montoPagado.multiply(BigDecimal.valueOf(100)).divide(valorMovil, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        dto.setNombreRegla(regla.getNombre());
        dto.setTipoRegla(tipo);
        dto.setCuotasPagadas(cuotasPagadas);
        dto.setMinimoCuotas(regla.getMinimoCuotas());
        dto.setPorcentajeIntegrado(porcentajeIntegrado);
        dto.setMinimoPorcentaje(regla.getMinimoPorcentaje());
        dto.setPermiteMora(Boolean.TRUE.equals(regla.getPermiteMora()));

        if (tipo == TipoReglaAdjudicacionPlan.MANUAL) {
            dto.setApto(false);
            dto.setMensaje("Este plan requiere adjudicaciÃ³n manual por administrador.");
            return dto;
        }

        if (contrato.getEstado() == EstadoContratoPlanAhorro.EN_MORA && !Boolean.TRUE.equals(regla.getPermiteMora())) {
            dto.setApto(false);
            dto.setMensaje("El contrato estÃ¡ en mora y la regla no permite adjudicar en este estado.");
            return dto;
        }

        if (Boolean.TRUE.equals(regla.getRequiereContratoActivo())) {
            boolean estadoValido =
                contrato.getEstado() == EstadoContratoPlanAhorro.ACTIVO ||
                contrato.getEstado() == EstadoContratoPlanAhorro.EN_MORA ||
                contrato.getEstado() == EstadoContratoPlanAhorro.FINALIZADO ||
                contrato.getEstado() == EstadoContratoPlanAhorro.ADJUDICADO;
            if (!estadoValido) {
                dto.setApto(false);
                dto.setMensaje("El estado del contrato no habilita adjudicaciÃ³n segÃºn la regla.");
                return dto;
            }
        }

        boolean cumpleCuotas = regla.getMinimoCuotas() == null || cuotasPagadas >= regla.getMinimoCuotas();
        boolean cumplePorcentaje = regla.getMinimoPorcentaje() == null || porcentajeIntegrado.compareTo(regla.getMinimoPorcentaje()) >= 0;

        boolean apto = switch (tipo) {
            case SIN_REGLA -> true;
            case POR_CUOTAS -> cumpleCuotas;
            case POR_PORCENTAJE -> cumplePorcentaje;
            case CUOTAS_O_PORCENTAJE -> cumpleCuotas || cumplePorcentaje;
            case CUOTAS_Y_PORCENTAJE -> cumpleCuotas && cumplePorcentaje;
            case MANUAL -> false;
        };

        dto.setApto(apto);
        dto.setMensaje(
            apto
                ? "Contrato apto para adjudicaciÃ³n."
                : "El contrato no cumple las condiciones mÃ­nimas de adjudicaciÃ³n para la regla configurada."
        );
        return dto;
    }

    public BigDecimal calcularMontoReconocidoCuotas(Long contratoId) {
        return cuotaRepository
            .findAllByContratoIdOrderByNumeroCuotaAsc(contratoId)
            .stream()
            .filter(c -> c.getEstado() == EstadoCuotaPlanAhorro.PAGADA)
            .map(CuotaPlanAhorro::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private ReglaAdjudicacionPlan reglaSinReglaDefault() {
        ReglaAdjudicacionPlan regla = new ReglaAdjudicacionPlan();
        regla.setNombre("SIN_REGLA_DEFAULT");
        regla.setTipoRegla(TipoReglaAdjudicacionPlan.SIN_REGLA);
        regla.setPermiteMora(true);
        regla.setRequiereContratoActivo(false);
        return regla;
    }
}

