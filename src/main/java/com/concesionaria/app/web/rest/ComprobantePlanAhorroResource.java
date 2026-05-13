package com.concesionaria.app.web.rest;

import com.concesionaria.app.service.ComprobantePlanAhorroService;
import com.concesionaria.app.service.dto.ComprobantePdfResult;
import com.concesionaria.app.service.dto.ComprobantePlanAhorroDTO;
import com.concesionaria.app.web.rest.vm.AnulacionRequestVM;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/comprobantes-plan-ahorro")
public class ComprobantePlanAhorroResource {

    private final ComprobantePlanAhorroService service;

    public ComprobantePlanAhorroResource(ComprobantePlanAhorroService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<ComprobantePlanAhorroDTO> getOne(@PathVariable("id") Long id) {
        return ResponseUtil.wrapOrNotFound(service.findOne(id));
    }

    @GetMapping("/by-cuota/{cuotaId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<List<ComprobantePlanAhorroDTO>> byCuota(@PathVariable("cuotaId") Long cuotaId) {
        return ResponseEntity.ok(service.findByCuota(cuotaId));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<byte[]> pdf(@PathVariable("id") Long id) {
        Optional<ComprobantePdfResult> result = service.generarPdf(id);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ComprobantePdfResult pdf = result.get();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdf.fileName() + "\"")
            .body(pdf.content());
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<ComprobantePlanAhorroDTO> anular(@PathVariable("id") Long id, @Valid @RequestBody AnulacionRequestVM request) {
        return ResponseEntity.ok(service.anular(id, request.getMotivo()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

