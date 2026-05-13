package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.PlanAhorroRepository;
import com.concesionaria.app.service.PlanAhorroService;
import com.concesionaria.app.service.dto.PlanAhorroDTO;
import com.concesionaria.app.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
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
@RequestMapping("/api/plan-ahorros")
public class PlanAhorroResource {

    private static final String ENTITY_NAME = "planAhorro";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final PlanAhorroService planAhorroService;
    private final PlanAhorroRepository planAhorroRepository;

    public PlanAhorroResource(PlanAhorroService planAhorroService, PlanAhorroRepository planAhorroRepository) {
        this.planAhorroService = planAhorroService;
        this.planAhorroRepository = planAhorroRepository;
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PlanAhorroDTO> create(@Valid @RequestBody PlanAhorroDTO dto) throws URISyntaxException {
        if (dto.getId() != null) {
            throw new BadRequestAlertException("A new planAhorro cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PlanAhorroDTO result = planAhorroService.save(dto);
        return ResponseEntity.created(new URI("/api/plan-ahorros/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PlanAhorroDTO> update(@PathVariable("id") Long id, @Valid @RequestBody PlanAhorroDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dto.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!planAhorroRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        PlanAhorroDTO result = planAhorroService.update(dto);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dto.getId().toString()))
            .body(result);
    }

    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<List<PlanAhorroDTO>> getAll(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<PlanAhorroDTO> page = planAhorroService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<PlanAhorroDTO> get(@PathVariable("id") Long id) {
        Optional<PlanAhorroDTO> dto = planAhorroService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        planAhorroService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}

