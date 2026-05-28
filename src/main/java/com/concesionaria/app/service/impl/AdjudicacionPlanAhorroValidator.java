package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.AdjudicacionPlanAhorro;
import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.domain.enumeration.EstadoAdjudicacionPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.repository.AdjudicacionPlanAhorroRepository;
import com.concesionaria.app.service.exception.BadRequestException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdjudicacionPlanAhorroValidator {

    private final AdjudicacionPlanAhorroRepository adjudicacionRepository;

    public AdjudicacionPlanAhorroValidator(AdjudicacionPlanAhorroRepository adjudicacionRepository) {
        this.adjudicacionRepository = adjudicacionRepository;
    }

    public void validarContratoAdjudicable(ContratoPlanAhorro contrato) {
        if (contrato.getEstado() == EstadoContratoPlanAhorro.CANCELADO) {
            throw new BadRequestException("No se puede adjudicar un contrato cancelado");
        }
    }

    public void validarNoDuplicarAdjudicacionActiva(Long contratoId) {
        if (
            adjudicacionRepository.existsByContratoIdAndEstadoIn(
                contratoId,
                List.of(EstadoAdjudicacionPlanAhorro.PENDIENTE, EstadoAdjudicacionPlanAhorro.ADJUDICADA, EstadoAdjudicacionPlanAhorro.VENTA_GENERADA)
            )
        ) {
            throw new BadRequestException("El contrato ya tiene una adjudicacion activa");
        }
    }

    public void validarEstadoPermiteAsignarInventario(AdjudicacionPlanAhorro adjudicacion) {
        if (adjudicacion.getEstado() == EstadoAdjudicacionPlanAhorro.CANCELADA || adjudicacion.getEstado() == EstadoAdjudicacionPlanAhorro.ENTREGADA) {
            throw new BadRequestException("La adjudicacion no permite asignar inventario");
        }
    }

    public void validarInventarioDisponible(Inventario inventario) {
        if (inventario.getEstadoInventario() != EstadoInventario.DISPONIBLE) {
            throw new BadRequestException("Solo se puede asignar inventario disponible");
        }
    }

    public void validarInventarioCompatible(ContratoPlanAhorro contrato, Inventario inventario) {
        if (!esInventarioCompatibleConPlan(contrato, inventario)) {
            throw new BadRequestException("El inventario seleccionado no es compatible con el plan");
        }
    }

    public void validarGeneracionVenta(AdjudicacionPlanAhorro adjudicacion) {
        if (adjudicacion.getInventario() == null || adjudicacion.getInventario().getId() == null) {
            throw new BadRequestException("Debe asignar inventario antes de generar la venta");
        }
    }

    public void validarEstadoPermiteCancelar(AdjudicacionPlanAhorro adjudicacion) {
        if (adjudicacion.getVenta() != null) {
            throw new BadRequestException("No se puede cancelar una adjudicacion que ya genero venta");
        }
    }

    public void validarEstadoPermiteMarcarEntregada(AdjudicacionPlanAhorro adjudicacion) {
        if (adjudicacion.getEstado() != EstadoAdjudicacionPlanAhorro.VENTA_GENERADA) {
            throw new BadRequestException("Solo se puede marcar entregada una adjudicacion con venta generada");
        }
    }

    private boolean esInventarioCompatibleConPlan(ContratoPlanAhorro contrato, Inventario inventario) {
        Version versionObjetivo = contrato.getPlan() != null ? contrato.getPlan().getVersionObjetivo() : null;
        if (versionObjetivo == null || versionObjetivo.getId() == null) {
            return true;
        }
        Version versionInventario = inventario.getVehiculo() != null ? inventario.getVehiculo().getVersion() : null;
        if (versionInventario == null) {
            return false;
        }
        if (versionObjetivo.getId().equals(versionInventario.getId())) {
            return true;
        }
        Long modeloObjetivoId = versionObjetivo.getModelo() != null ? versionObjetivo.getModelo().getId() : null;
        Long modeloInventarioId = versionInventario.getModelo() != null ? versionInventario.getModelo().getId() : null;
        return modeloObjetivoId != null && modeloObjetivoId.equals(modeloInventarioId);
    }
}

