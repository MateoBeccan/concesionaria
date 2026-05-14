package com.concesionaria.app.web.rest;

import com.concesionaria.app.domain.enumeration.EstadoEntregaUnidad;
import com.concesionaria.app.service.EntregaUnidadService;
import com.concesionaria.app.service.dto.EntregaChecklistItemDTO;
import com.concesionaria.app.service.dto.EntregaUnidadDTO;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/entregas-unidad")
public class EntregaUnidadResource {

    private static final String ENTITY_NAME = "entregaUnidad";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final EntregaUnidadService entregaUnidadService;

    public EntregaUnidadResource(EntregaUnidadService entregaUnidadService) {
        this.entregaUnidadService = entregaUnidadService;
    }

    @PostMapping("/ventas/{ventaId}/programar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<EntregaUnidadDTO> programarEntrega(
        @PathVariable("ventaId") Long ventaId,
        @RequestBody(required = false) Map<String, Object> payload
    ) throws URISyntaxException {
        Instant fechaProgramada = payload != null && payload.get("fechaProgramada") != null
            ? Instant.parse(String.valueOf(payload.get("fechaProgramada")))
            : null;
        String observaciones = payload != null ? (String) payload.get("observaciones") : null;
        EntregaUnidadDTO dto = entregaUnidadService.programarEntrega(ventaId, fechaProgramada, observaciones);
        return ResponseEntity.created(new URI("/api/entregas-unidad/" + dto.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, dto.getId().toString()))
            .body(dto);
    }

    @PutMapping("/{entregaId}/checklist")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<EntregaUnidadDTO> actualizarChecklist(
        @PathVariable("entregaId") Long entregaId,
        @RequestBody List<EntregaChecklistItemDTO> items
    ) {
        return ResponseEntity.ok(entregaUnidadService.actualizarChecklist(entregaId, items));
    }

    @PostMapping("/{entregaId}/confirmar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<EntregaUnidadDTO> confirmarEntrega(
        @PathVariable("entregaId") Long entregaId,
        @RequestBody(required = false) Map<String, Object> payload
    ) {
        Integer kilometraje = payload != null && payload.get("kilometrajeEntrega") != null ? Integer.valueOf(String.valueOf(payload.get("kilometrajeEntrega"))) : null;
        String nivelCombustible = payload != null ? (String) payload.get("nivelCombustible") : null;
        String observaciones = payload != null ? (String) payload.get("observaciones") : null;
        return ResponseEntity.ok(entregaUnidadService.confirmarEntrega(entregaId, kilometraje, nivelCombustible, observaciones));
    }

    @PostMapping("/{entregaId}/cancelar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<EntregaUnidadDTO> cancelarEntrega(
        @PathVariable("entregaId") Long entregaId,
        @RequestBody(required = false) Map<String, String> payload
    ) {
        String motivo = payload != null ? payload.get("motivo") : null;
        return ResponseEntity.ok(entregaUnidadService.cancelarEntrega(entregaId, motivo));
    }

    @GetMapping("/venta/{ventaId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<EntregaUnidadDTO> findByVentaId(@PathVariable("ventaId") Long ventaId) {
        return ResponseUtil.wrapOrNotFound(entregaUnidadService.findByVentaId(ventaId));
    }

    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<List<EntregaUnidadDTO>> findAll(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(value = "estado", required = false) EstadoEntregaUnidad estado,
        @RequestParam(value = "fromDate", required = false) Instant fromDate,
        @RequestParam(value = "toDate", required = false) Instant toDate,
        @RequestParam(value = "cliente", required = false) String cliente,
        @RequestParam(value = "ventaId", required = false) Long ventaId
    ) {
        Page<EntregaUnidadDTO> page = entregaUnidadService.search(pageable, estado, fromDate, toDate, cliente, ventaId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}

