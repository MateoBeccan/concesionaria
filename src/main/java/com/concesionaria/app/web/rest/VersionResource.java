package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.VersionRepository;
import com.concesionaria.app.service.VersionQueryService;
import com.concesionaria.app.service.VersionService;
import com.concesionaria.app.service.criteria.VersionCriteria;
import com.concesionaria.app.service.dto.VersionDTO;
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
 * REST controller for managing {@link com.concesionaria.app.domain.Version}.
 */
@RestController
@RequestMapping("/api/versions")
public class VersionResource {

    private static final Logger LOG = LoggerFactory.getLogger(VersionResource.class);

    private static final String ENTITY_NAME = "version";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final VersionService versionService;

    private final VersionRepository versionRepository;

    private final VersionQueryService versionQueryService;

    public VersionResource(VersionService versionService, VersionRepository versionRepository, VersionQueryService versionQueryService) {
        this.versionService = versionService;
        this.versionRepository = versionRepository;
        this.versionQueryService = versionQueryService;
    }

    /**
     * {@code POST  /versions} : Create a new version.
     *
     * @param versionDTO the versionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new versionDTO, or with status {@code 400 (Bad Request)} if the version has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<VersionDTO> createVersion(@Valid @RequestBody VersionDTO versionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Version : {}", versionDTO);
        if (versionDTO.getId() != null) {
            throw new BadRequestAlertException("A new version cannot already have an ID", ENTITY_NAME, "idexists");
        }
        versionDTO = versionService.save(versionDTO);
        return ResponseEntity.created(new URI("/api/versions/" + versionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, versionDTO.getId().toString()))
            .body(versionDTO);
    }

    /**
     * {@code PUT  /versions/:id} : Updates an existing version.
     *
     * @param id the id of the versionDTO to save.
     * @param versionDTO the versionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated versionDTO,
     * or with status {@code 400 (Bad Request)} if the versionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the versionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VersionDTO> updateVersion(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody VersionDTO versionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Version : {}, {}", id, versionDTO);
        if (versionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, versionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!versionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        versionDTO = versionService.update(versionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, versionDTO.getId().toString()))
            .body(versionDTO);
    }

    /**
     * {@code PATCH  /versions/:id} : Partial updates given fields of an existing version, field will ignore if it is null
     *
     * @param id the id of the versionDTO to save.
     * @param versionDTO the versionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated versionDTO,
     * or with status {@code 400 (Bad Request)} if the versionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the versionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the versionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<VersionDTO> partialUpdateVersion(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VersionDTO versionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Version partially : {}, {}", id, versionDTO);
        if (versionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, versionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!versionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<VersionDTO> result = versionService.partialUpdate(versionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, versionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /versions} : get all the Versions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Versions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<VersionDTO>> getAllVersions(
        VersionCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Versions by criteria: {}", criteria);

        Page<VersionDTO> page = versionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /versions/count} : count all the versions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countVersions(VersionCriteria criteria) {
        LOG.debug("REST request to count Versions by criteria: {}", criteria);
        return ResponseEntity.ok().body(versionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /versions/:id} : get the "id" version.
     *
     * @param id the id of the versionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the versionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VersionDTO> getVersion(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Version : {}", id);
        Optional<VersionDTO> versionDTO = versionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(versionDTO);
    }

    /**
     * {@code DELETE  /versions/:id} : delete the "id" version.
     *
     * @param id the id of the versionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVersion(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Version : {}", id);
        versionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
