package com.concesionaria.app.web.rest;

import com.concesionaria.app.service.AdjudicacionPlanAhorroService;
import com.concesionaria.app.service.dto.AdjudicacionPlanAhorroDTO;
import com.concesionaria.app.service.dto.InventarioDTO;
import com.concesionaria.app.security.SecurityUtils;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/adjudicaciones-plan-ahorro")
public class AdjudicacionPlanAhorroResource {

    private final AdjudicacionPlanAhorroService adjudicacionService;

    public AdjudicacionPlanAhorroResource(AdjudicacionPlanAhorroService adjudicacionService) {
        this.adjudicacionService = adjudicacionService;
    }

    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<List<AdjudicacionPlanAhorroDTO>> getAll(Pageable pageable) {
        Page<AdjudicacionPlanAhorroDTO> page = SecurityUtils.hasCurrentUserAnyOfAuthorities("ROLE_ADMIN")
            ? adjudicacionService.findAll(pageable)
            : adjudicacionService.findAllCurrentUser(pageable);
        return ResponseEntity.ok()
            .headers(PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page))
            .body(page.getContent());
    }

    @PostMapping("/contratos/{contratoId}/adjudicar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<AdjudicacionPlanAhorroDTO> adjudicarContrato(
        @PathVariable("contratoId") Long contratoId,
        @RequestBody(required = false) Map<String, String> payload
    ) {
        String observaciones = payload == null ? null : payload.get("observaciones");
        return ResponseEntity.ok(adjudicacionService.adjudicarContrato(contratoId, observaciones));
    }

    @PostMapping("/{adjudicacionId}/inventario/{inventarioId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<AdjudicacionPlanAhorroDTO> asignarInventario(
        @PathVariable("adjudicacionId") Long adjudicacionId,
        @PathVariable("inventarioId") Long inventarioId
    ) {
        return ResponseEntity.ok(adjudicacionService.asignarInventario(adjudicacionId, inventarioId));
    }

    @PostMapping("/{adjudicacionId}/generar-venta")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<AdjudicacionPlanAhorroDTO> generarVenta(@PathVariable("adjudicacionId") Long adjudicacionId) {
        return ResponseEntity.ok(adjudicacionService.generarVenta(adjudicacionId));
    }

    @PostMapping("/{adjudicacionId}/cancelar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<AdjudicacionPlanAhorroDTO> cancelar(
        @PathVariable("adjudicacionId") Long adjudicacionId,
        @RequestBody(required = false) Map<String, String> payload
    ) {
        String motivo = payload == null ? null : payload.get("motivo");
        return ResponseEntity.ok(adjudicacionService.cancelarAdjudicacion(adjudicacionId, motivo));
    }

    @PostMapping("/{adjudicacionId}/entregar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<AdjudicacionPlanAhorroDTO> marcarEntregada(@PathVariable("adjudicacionId") Long adjudicacionId) {
        return ResponseEntity.ok(adjudicacionService.marcarEntregada(adjudicacionId));
    }

    @GetMapping("/contratos/{contratoId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<AdjudicacionPlanAhorroDTO> getLatestByContrato(@PathVariable("contratoId") Long contratoId) {
        return ResponseUtil.wrapOrNotFound(adjudicacionService.findLatestByContrato(contratoId));
    }

    @GetMapping("/contratos/{contratoId}/inventarios-compatibles")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<List<InventarioDTO>> getInventarioCompatible(@PathVariable("contratoId") Long contratoId) {
        return ResponseEntity.ok(adjudicacionService.findInventarioCompatibleDisponible(contratoId));
    }
}
