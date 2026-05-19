package com.concesionaria.app.web.rest;

import com.concesionaria.app.service.ReglaAdjudicacionPlanService;
import com.concesionaria.app.service.dto.ReglaAdjudicacionPlanDTO;
import com.concesionaria.app.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/reglas-adjudicacion-plan")
public class ReglaAdjudicacionPlanResource {

    private static final String ENTITY_NAME = "reglaAdjudicacionPlan";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final ReglaAdjudicacionPlanService service;

    public ReglaAdjudicacionPlanResource(ReglaAdjudicacionPlanService service) {
        this.service = service;
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReglaAdjudicacionPlanDTO> create(@Valid @RequestBody ReglaAdjudicacionPlanDTO dto) throws URISyntaxException {
        if (dto.getId() != null) {
            throw new BadRequestAlertException("A new reglaAdjudicacionPlan cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ReglaAdjudicacionPlanDTO result = service.save(dto);
        return ResponseEntity.created(new URI("/api/reglas-adjudicacion-plan/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReglaAdjudicacionPlanDTO> update(@PathVariable("id") Long id, @Valid @RequestBody ReglaAdjudicacionPlanDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dto.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        ReglaAdjudicacionPlanDTO result = service.update(dto);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dto.getId().toString()))
            .body(result);
    }

    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<List<ReglaAdjudicacionPlanDTO>> getAll(@RequestParam(name = "active", required = false) Boolean active) {
        return ResponseEntity.ok(service.findAll(active));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<ReglaAdjudicacionPlanDTO> get(@PathVariable("id") Long id) {
        return ResponseUtil.wrapOrNotFound(service.findOne(id));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReglaAdjudicacionPlanDTO> deactivate(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.deactivate(id));
    }
}
