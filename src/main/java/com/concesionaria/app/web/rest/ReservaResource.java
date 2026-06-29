package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.security.AuthoritiesConstants;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.ReservaService;
import com.concesionaria.app.service.dto.ReservaDTO;
import com.concesionaria.app.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/reservas")
public class ReservaResource {

    private static final String ENTITY_NAME = "reserva";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final ReservaService reservaService;
    private final ReservaRepository reservaRepository;

    public ReservaResource(ReservaService reservaService, ReservaRepository reservaRepository) {
        this.reservaService = reservaService;
        this.reservaRepository = reservaRepository;
    }

    @PostMapping("")
    public ResponseEntity<ReservaDTO> create(@Valid @RequestBody ReservaDTO dto) throws URISyntaxException {
        if (dto.getId() != null) {
            throw new BadRequestAlertException("A new reserva cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ReservaDTO result = reservaService.save(dto);
        return ResponseEntity.created(new URI("/api/reservas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservaDTO> update(@PathVariable Long id, @Valid @RequestBody ReservaDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dto.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!reservaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        ReservaDTO result = reservaService.update(dto);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, id.toString())).body(result);
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ReservaDTO> partialUpdate(@PathVariable Long id, @NotNull @RequestBody ReservaDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dto.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!reservaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        Optional<ReservaDTO> result = reservaService.partialUpdate(dto);
        return ResponseUtil.wrapOrNotFound(result, HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, id.toString()));
    }

    @GetMapping("")
    public ResponseEntity<List<ReservaDTO>> getAll(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        boolean isAdmin = SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN);
        Page<ReservaDTO> page = isAdmin ? reservaService.findAll(pageable) : reservaService.findAllCurrentUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> getOne(@PathVariable Long id) {
        return ResponseUtil.wrapOrNotFound(reservaService.findOne(id));
    }

    @PostMapping("/expirar-vencidas")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Map<String, Long>> expirarVencidas() {
        long total = reservaService.expirarReservasVencidas();
        return ResponseEntity.ok(Map.of("reservasExpiradas", total));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ReservaDTO> cancelar(@PathVariable Long id, @RequestParam(value = "motivo", required = false) String motivo) {
        return ResponseEntity.ok(reservaService.cancelarReserva(id, motivo));
    }

    @GetMapping("/inventario/{inventarioId}/activa")
    public ResponseEntity<ReservaDTO> getActivaByInventario(@PathVariable Long inventarioId) {
        return ResponseUtil.wrapOrNotFound(reservaService.findActivaByInventarioId(inventarioId));
    }
}

