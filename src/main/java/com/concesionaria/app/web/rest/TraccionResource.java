package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.TraccionRepository;
import com.concesionaria.app.service.TraccionService;
import com.concesionaria.app.service.dto.TraccionDTO;
import com.concesionaria.app.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * REST controller for managing {@link com.concesionaria.app.domain.Traccion}.
 */
@RestController
@RequestMapping("/api/traccions")
public class TraccionResource {

    private static final Logger LOG = LoggerFactory.getLogger(TraccionResource.class);

    private static final String ENTITY_NAME = "traccion";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final TraccionService traccionService;

    private final TraccionRepository traccionRepository;

    public TraccionResource(TraccionService traccionService, TraccionRepository traccionRepository) {
        this.traccionService = traccionService;
        this.traccionRepository = traccionRepository;
    }

    /**
     * {@code POST  /traccions} : Create a new traccion.
     *
     * @param traccionDTO the traccionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new traccionDTO, or with status {@code 400 (Bad Request)} if the traccion has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TraccionDTO> createTraccion(@Valid @RequestBody TraccionDTO traccionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Traccion : {}", traccionDTO);
        if (traccionDTO.getId() != null) {
            throw new BadRequestAlertException("A new traccion cannot already have an ID", ENTITY_NAME, "idexists");
        }
        traccionDTO = traccionService.save(traccionDTO);
        return ResponseEntity.created(new URI("/api/traccions/" + traccionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, traccionDTO.getId().toString()))
            .body(traccionDTO);
    }

    /**
     * {@code PUT  /traccions/:id} : Updates an existing traccion.
     *
     * @param id the id of the traccionDTO to save.
     * @param traccionDTO the traccionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated traccionDTO,
     * or with status {@code 400 (Bad Request)} if the traccionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the traccionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TraccionDTO> updateTraccion(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TraccionDTO traccionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Traccion : {}, {}", id, traccionDTO);
        if (traccionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, traccionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!traccionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        traccionDTO = traccionService.update(traccionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, traccionDTO.getId().toString()))
            .body(traccionDTO);
    }

    /**
     * {@code PATCH  /traccions/:id} : Partial updates given fields of an existing traccion, field will ignore if it is null
     *
     * @param id the id of the traccionDTO to save.
     * @param traccionDTO the traccionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated traccionDTO,
     * or with status {@code 400 (Bad Request)} if the traccionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the traccionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the traccionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TraccionDTO> partialUpdateTraccion(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TraccionDTO traccionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Traccion partially : {}, {}", id, traccionDTO);
        if (traccionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, traccionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!traccionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TraccionDTO> result = traccionService.partialUpdate(traccionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, traccionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /traccions} : get all the Traccions.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Traccions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TraccionDTO>> getAllTraccions(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Traccions");
        Page<TraccionDTO> page = traccionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /traccions/:id} : get the "id" traccion.
     *
     * @param id the id of the traccionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the traccionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TraccionDTO> getTraccion(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Traccion : {}", id);
        Optional<TraccionDTO> traccionDTO = traccionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(traccionDTO);
    }

    /**
     * {@code DELETE  /traccions/:id} : delete the "id" traccion.
     *
     * @param id the id of the traccionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraccion(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Traccion : {}", id);
        traccionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
