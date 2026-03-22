package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.AutoRepository;
import com.concesionaria.app.service.AutoQueryService;
import com.concesionaria.app.service.AutoService;
import com.concesionaria.app.service.criteria.AutoCriteria;
import com.concesionaria.app.service.dto.AutoDTO;
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
 * REST controller for managing {@link com.concesionaria.app.domain.Auto}.
 */
@RestController
@RequestMapping("/api/autos")
public class AutoResource {

    private static final Logger LOG = LoggerFactory.getLogger(AutoResource.class);

    private static final String ENTITY_NAME = "auto";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final AutoService autoService;

    private final AutoRepository autoRepository;

    private final AutoQueryService autoQueryService;

    public AutoResource(AutoService autoService, AutoRepository autoRepository, AutoQueryService autoQueryService) {
        this.autoService = autoService;
        this.autoRepository = autoRepository;
        this.autoQueryService = autoQueryService;
    }

    /**
     * {@code POST  /autos} : Create a new auto.
     *
     * @param autoDTO the autoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new autoDTO, or with status {@code 400 (Bad Request)} if the auto has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AutoDTO> createAuto(@Valid @RequestBody AutoDTO autoDTO) throws URISyntaxException {
        LOG.debug("REST request to save Auto : {}", autoDTO);
        if (autoDTO.getId() != null) {
            throw new BadRequestAlertException("A new auto cannot already have an ID", ENTITY_NAME, "idexists");
        }
        autoDTO = autoService.save(autoDTO);
        return ResponseEntity.created(new URI("/api/autos/" + autoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, autoDTO.getId().toString()))
            .body(autoDTO);
    }

    /**
     * {@code PUT  /autos/:id} : Updates an existing auto.
     *
     * @param id the id of the autoDTO to save.
     * @param autoDTO the autoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated autoDTO,
     * or with status {@code 400 (Bad Request)} if the autoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the autoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AutoDTO> updateAuto(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AutoDTO autoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Auto : {}, {}", id, autoDTO);
        if (autoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, autoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!autoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        autoDTO = autoService.update(autoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, autoDTO.getId().toString()))
            .body(autoDTO);
    }

    /**
     * {@code PATCH  /autos/:id} : Partial updates given fields of an existing auto, field will ignore if it is null
     *
     * @param id the id of the autoDTO to save.
     * @param autoDTO the autoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated autoDTO,
     * or with status {@code 400 (Bad Request)} if the autoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the autoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the autoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AutoDTO> partialUpdateAuto(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AutoDTO autoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Auto partially : {}, {}", id, autoDTO);
        if (autoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, autoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!autoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AutoDTO> result = autoService.partialUpdate(autoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, autoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /autos} : get all the Autos.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Autos in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AutoDTO>> getAllAutos(
        AutoCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Autos by criteria: {}", criteria);

        Page<AutoDTO> page = autoQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /autos/count} : count all the autos.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAutos(AutoCriteria criteria) {
        LOG.debug("REST request to count Autos by criteria: {}", criteria);
        return ResponseEntity.ok().body(autoQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /autos/:id} : get the "id" auto.
     *
     * @param id the id of the autoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the autoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AutoDTO> getAuto(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Auto : {}", id);
        Optional<AutoDTO> autoDTO = autoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(autoDTO);
    }

    /**
     * {@code DELETE  /autos/:id} : delete the "id" auto.
     *
     * @param id the id of the autoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuto(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Auto : {}", id);
        autoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
