package com.concesionaria.app.service;

import com.concesionaria.app.domain.enumeration.EstadoEntregaUnidad;
import com.concesionaria.app.service.dto.EntregaChecklistItemDTO;
import com.concesionaria.app.service.dto.EntregaUnidadDTO;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EntregaUnidadService {
    EntregaUnidadDTO programarEntrega(Long ventaId, Instant fechaProgramada, String observaciones);

    EntregaUnidadDTO actualizarChecklist(Long entregaId, List<EntregaChecklistItemDTO> items);

    EntregaUnidadDTO confirmarEntrega(Long entregaId, Integer kilometrajeEntrega, String nivelCombustible, String observaciones);

    EntregaUnidadDTO cancelarEntrega(Long entregaId, String motivo);

    Optional<EntregaUnidadDTO> findByVentaId(Long ventaId);

    Page<EntregaUnidadDTO> findAll(Pageable pageable);

    Page<EntregaUnidadDTO> search(
        Pageable pageable,
        EstadoEntregaUnidad estado,
        Instant fromDate,
        Instant toDate,
        String cliente,
        Long ventaId
    );
}

