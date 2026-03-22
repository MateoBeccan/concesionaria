package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.CombustibleRepository;
import com.concesionaria.app.service.CombustibleQueryService;
import com.concesionaria.app.service.CombustibleService;
import com.concesionaria.app.service.criteria.CombustibleCriteria;
import com.concesionaria.app.service.dto.CombustibleDTO;
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
 * REST controller for managing {@link com.concesionaria.app.domain.Combustible}.
 */
@RestController
@RequestMapping("/api/combustibles")
public class CombustibleResource {

    private static final Logger LOG = LoggerFactory.getLogger(CombustibleResource.class);

    private static final String ENTITY_NAME = "combustible";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final CombustibleService combustibleService;

    private final CombustibleRepository combustibleRepository;

    private final CombustibleQueryService combustibleQueryService;

    public CombustibleResource(
        CombustibleService combustibleService,
        CombustibleRepository combustibleRepository,
        CombustibleQueryService combustibleQueryService
    ) {
        this.combustibleService = combustibleService;
        this.combustibleRepository = combustibleRepository;
        this.combustibleQueryService = combustibleQueryService;
    }

    /**
     * {@code POST  /combustibles} : Create a new combustible.
     *
     * @param combustibleDTO the combustibleDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new combustibleDTO, or with status {@code 400 (Bad Request)} if the combustible has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CombustibleDTO> createCombustible(@Valid @RequestBody CombustibleDTO combustibleDTO) throws URISyntaxException {
        LOG.debug("REST request to save Combustible : {}", combustibleDTO);
        if (combustibleDTO.getId() != null) {
            throw new BadRequestAlertException("A new combustible cannot already have an ID", ENTITY_NAME, "idexists");
        }
        combustibleDTO = combustibleService.save(combustibleDTO);
        return ResponseEntity.created(new URI("/api/combustibles/" + combustibleDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, combustibleDTO.getId().toString()))
            .body(combustibleDTO);
    }

    /**
     * {@code PUT  /combustibles/:id} : Updates an existing combustible.
     *
     * @param id the id of the combustibleDTO to save.
     * @param combustibleDTO the combustibleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated combustibleDTO,
     * or with status {@code 400 (Bad Request)} if the combustibleDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the combustibleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CombustibleDTO> updateCombustible(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CombustibleDTO combustibleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Combustible : {}, {}", id, combustibleDTO);
        if (combustibleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, combustibleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!combustibleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        combustibleDTO = combustibleService.update(combustibleDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, combustibleDTO.getId().toString()))
            .body(combustibleDTO);
    }

    /**
     * {@code PATCH  /combustibles/:id} : Partial updates given fields of an existing combustible, field will ignore if it is null
     *
     * @param id the id of the combustibleDTO to save.
     * @param combustibleDTO the combustibleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated combustibleDTO,
     * or with status {@code 400 (Bad Request)} if the combustibleDTO is not valid,
     * or with status {@code 404 (Not Found)} if the combustibleDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the combustibleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CombustibleDTO> partialUpdateCombustible(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CombustibleDTO combustibleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Combustible partially : {}, {}", id, combustibleDTO);
        if (combustibleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, combustibleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!combustibleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CombustibleDTO> result = combustibleService.partialUpdate(combustibleDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, combustibleDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /combustibles} : get all the Combustibles.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Combustibles in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CombustibleDTO>> getAllCombustibles(
        CombustibleCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Combustibles by criteria: {}", criteria);

        Page<CombustibleDTO> page = combustibleQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /combustibles/count} : count all the combustibles.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countCombustibles(CombustibleCriteria criteria) {
        LOG.debug("REST request to count Combustibles by criteria: {}", criteria);
        return ResponseEntity.ok().body(combustibleQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /combustibles/:id} : get the "id" combustible.
     *
     * @param id the id of the combustibleDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the combustibleDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CombustibleDTO> getCombustible(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Combustible : {}", id);
        Optional<CombustibleDTO> combustibleDTO = combustibleService.findOne(id);
        return ResponseUtil.wrapOrNotFound(combustibleDTO);
    }

    /**
     * {@code DELETE  /combustibles/:id} : delete the "id" combustible.
     *
     * @param id the id of the combustibleDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCombustible(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Combustible : {}", id);
        combustibleService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
