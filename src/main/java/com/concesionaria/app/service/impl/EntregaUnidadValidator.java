package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.EntregaChecklistItem;
import com.concesionaria.app.domain.EntregaUnidad;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoEntregaUnidad;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.EntregaUnidadRepository;
import com.concesionaria.app.service.exception.BadRequestException;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EntregaUnidadValidator {

    private final EntregaUnidadRepository entregaUnidadRepository;

    public EntregaUnidadValidator(EntregaUnidadRepository entregaUnidadRepository) {
        this.entregaUnidadRepository = entregaUnidadRepository;
    }

    public void validarVentaEntregable(Venta venta) {
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new BadRequestException("No se puede programar entrega para una venta cancelada");
        }
        if (venta.getEstado() != EstadoVenta.PAGADA && venta.getEstado() != EstadoVenta.FINALIZADA) {
            throw new BadRequestException("Solo se puede programar entrega para venta PAGADA o FINALIZADA");
        }
    }

    public void validarNoDuplicarEntregaActiva(Long ventaId) {
        if (
            entregaUnidadRepository.existsByVentaIdAndEstadoIn(
                ventaId,
                EnumSet.of(EstadoEntregaUnidad.PENDIENTE, EstadoEntregaUnidad.PROGRAMADA, EstadoEntregaUnidad.ENTREGADA)
            )
        ) {
            throw new BadRequestException("La venta ya tiene una entrega activa");
        }
    }

    public void validarEstadoPermiteActualizarChecklist(EntregaUnidad entrega) {
        if (entrega.getEstado() == EstadoEntregaUnidad.ENTREGADA || entrega.getEstado() == EstadoEntregaUnidad.CANCELADA) {
            throw new BadRequestException("No se puede modificar checklist para una entrega cerrada");
        }
    }

    public void validarEstadoPermiteConfirmar(EntregaUnidad entrega) {
        if (entrega.getEstado() == EstadoEntregaUnidad.CANCELADA) {
            throw new BadRequestException("La entrega esta cancelada");
        }
    }

    public void validarChecklistObligatorioCompleto(List<EntregaChecklistItem> checklist) {
        boolean pendientesObligatorios = checklist
            .stream()
            .anyMatch(i -> Boolean.TRUE.equals(i.getObligatorio()) && !Boolean.TRUE.equals(i.getCompletado()));
        if (pendientesObligatorios) {
            throw new BadRequestException("No se puede confirmar entrega con checklist obligatorio incompleto");
        }
    }

    public void validarEstadoPermiteCancelar(EntregaUnidad entrega) {
        if (entrega.getEstado() == EstadoEntregaUnidad.ENTREGADA) {
            throw new BadRequestException("No se puede cancelar una entrega ya confirmada");
        }
    }
}

