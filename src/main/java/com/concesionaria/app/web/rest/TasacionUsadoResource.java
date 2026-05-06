package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.TasacionUsadoRepository;
import com.concesionaria.app.security.AuthoritiesConstants;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.TasacionUsadoService;
import com.concesionaria.app.service.dto.TasacionUsadoDTO;
import com.concesionaria.app.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/tasacion-usados")
public class TasacionUsadoResource {

    private static final String ENTITY_NAME = "tasacionUsado";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final TasacionUsadoService tasacionUsadoService;
    private final TasacionUsadoRepository tasacionUsadoRepository;

    public TasacionUsadoResource(TasacionUsadoService tasacionUsadoService, TasacionUsadoRepository tasacionUsadoRepository) {
        this.tasacionUsadoService = tasacionUsadoService;
        this.tasacionUsadoRepository = tasacionUsadoRepository;
    }

    @PostMapping("")
    public ResponseEntity<TasacionUsadoDTO> create(@Valid @RequestBody TasacionUsadoDTO dto) throws URISyntaxException {
        if (dto.getId() != null) {
            throw new BadRequestAlertException("A new tasacionUsado cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TasacionUsadoDTO result = tasacionUsadoService.save(dto);
        return ResponseEntity.created(new URI("/api/tasacion-usados/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TasacionUsadoDTO> update(@PathVariable Long id, @Valid @RequestBody TasacionUsadoDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dto.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!tasacionUsadoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        TasacionUsadoDTO result = tasacionUsadoService.update(dto);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, id.toString())).body(result);
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TasacionUsadoDTO> partialUpdate(@PathVariable Long id, @NotNull @RequestBody TasacionUsadoDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dto.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!tasacionUsadoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        Optional<TasacionUsadoDTO> result = tasacionUsadoService.partialUpdate(dto);
        return ResponseUtil.wrapOrNotFound(result, HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, id.toString()));
    }

    @GetMapping("")
    public ResponseEntity<List<TasacionUsadoDTO>> getAll(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        boolean isAdmin = SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN);
        Page<TasacionUsadoDTO> page = isAdmin ? tasacionUsadoService.findAll(pageable) : tasacionUsadoService.findAllCurrentUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TasacionUsadoDTO> getOne(@PathVariable Long id) {
        return ResponseUtil.wrapOrNotFound(tasacionUsadoService.findOne(id));
    }

    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<List<TasacionUsadoDTO>> getByVenta(@PathVariable Long ventaId) {
        return ResponseEntity.ok(tasacionUsadoService.findByVentaId(ventaId));
    }

    @GetMapping("/aceptadas-disponibles")
    public ResponseEntity<List<TasacionUsadoDTO>> getAceptadasDisponibles(@RequestParam Long clienteId) {
        return ResponseEntity.ok(tasacionUsadoService.findAceptadasDisponiblesByClienteId(clienteId));
    }
}
