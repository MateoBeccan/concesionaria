package com.concesionaria.app.web.rest;

import com.concesionaria.app.service.ContratoPlanAhorroService;
import com.concesionaria.app.service.dto.ContratoPlanAhorroDTO;
import com.concesionaria.app.service.dto.CuotaPlanAhorroDTO;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@RequestMapping("/api/contrato-plan-ahorros")
public class ContratoPlanAhorroResource {

    private static final String ENTITY_NAME = "contratoPlanAhorro";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final ContratoPlanAhorroService contratoService;

    public ContratoPlanAhorroResource(ContratoPlanAhorroService contratoService) {
        this.contratoService = contratoService;
    }

    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<ContratoPlanAhorroDTO> create(@Valid @RequestBody ContratoPlanAhorroDTO dto) throws URISyntaxException {
        ContratoPlanAhorroDTO result = contratoService.crearContrato(dto);
        return ResponseEntity.created(new URI("/api/contrato-plan-ahorros/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<List<ContratoPlanAhorroDTO>> getAll(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<ContratoPlanAhorroDTO> page = contratoService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<ContratoPlanAhorroDTO> get(@PathVariable("id") Long id) {
        Optional<ContratoPlanAhorroDTO> dto = contratoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dto);
    }

    @GetMapping("/{id}/cuotas")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<List<CuotaPlanAhorroDTO>> getCuotas(@PathVariable("id") Long contratoId) {
        return ResponseEntity.ok(contratoService.findCuotas(contratoId));
    }

    @PostMapping("/cuotas/{cuotaId}/pagar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<CuotaPlanAhorroDTO> pagarCuota(@PathVariable("cuotaId") Long cuotaId, @RequestBody Map<String, Object> payload) {
        BigDecimal monto = payload.get("monto") == null ? null : new BigDecimal(String.valueOf(payload.get("monto")));
        String observaciones = payload.get("observaciones") == null ? null : String.valueOf(payload.get("observaciones"));
        return ResponseEntity.ok(contratoService.pagarCuota(cuotaId, monto, observaciones));
    }
}

