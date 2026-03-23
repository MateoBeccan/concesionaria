package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.CondicionIvaRepository;
import com.concesionaria.app.service.CondicionIvaService;
import com.concesionaria.app.service.dto.CondicionIvaDTO;
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
 * REST controller for managing {@link com.concesionaria.app.domain.CondicionIva}.
 */
@RestController
@RequestMapping("/api/condicion-ivas")
public class CondicionIvaResource {

    private static final Logger LOG = LoggerFactory.getLogger(CondicionIvaResource.class);

    private static final String ENTITY_NAME = "condicionIva";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final CondicionIvaService condicionIvaService;

    private final CondicionIvaRepository condicionIvaRepository;

    public CondicionIvaResource(CondicionIvaService condicionIvaService, CondicionIvaRepository condicionIvaRepository) {
        this.condicionIvaService = condicionIvaService;
        this.condicionIvaRepository = condicionIvaRepository;
    }

    /**
     * {@code POST  /condicion-ivas} : Create a new condicionIva.
     *
     * @param condicionIvaDTO the condicionIvaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new condicionIvaDTO, or with status {@code 400 (Bad Request)} if the condicionIva has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CondicionIvaDTO> createCondicionIva(@Valid @RequestBody CondicionIvaDTO condicionIvaDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save CondicionIva : {}", condicionIvaDTO);
        if (condicionIvaDTO.getId() != null) {
            throw new BadRequestAlertException("A new condicionIva cannot already have an ID", ENTITY_NAME, "idexists");
        }
        condicionIvaDTO = condicionIvaService.save(condicionIvaDTO);
        return ResponseEntity.created(new URI("/api/condicion-ivas/" + condicionIvaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, condicionIvaDTO.getId().toString()))
            .body(condicionIvaDTO);
    }

    /**
     * {@code PUT  /condicion-ivas/:id} : Updates an existing condicionIva.
     *
     * @param id the id of the condicionIvaDTO to save.
     * @param condicionIvaDTO the condicionIvaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated condicionIvaDTO,
     * or with status {@code 400 (Bad Request)} if the condicionIvaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the condicionIvaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CondicionIvaDTO> updateCondicionIva(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CondicionIvaDTO condicionIvaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update CondicionIva : {}, {}", id, condicionIvaDTO);
        if (condicionIvaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, condicionIvaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!condicionIvaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        condicionIvaDTO = condicionIvaService.update(condicionIvaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, condicionIvaDTO.getId().toString()))
            .body(condicionIvaDTO);
    }

    /**
     * {@code PATCH  /condicion-ivas/:id} : Partial updates given fields of an existing condicionIva, field will ignore if it is null
     *
     * @param id the id of the condicionIvaDTO to save.
     * @param condicionIvaDTO the condicionIvaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated condicionIvaDTO,
     * or with status {@code 400 (Bad Request)} if the condicionIvaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the condicionIvaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the condicionIvaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CondicionIvaDTO> partialUpdateCondicionIva(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CondicionIvaDTO condicionIvaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update CondicionIva partially : {}, {}", id, condicionIvaDTO);
        if (condicionIvaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, condicionIvaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!condicionIvaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CondicionIvaDTO> result = condicionIvaService.partialUpdate(condicionIvaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, condicionIvaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /condicion-ivas} : get all the Condicion Ivas.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Condicion Ivas in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CondicionIvaDTO>> getAllCondicionIvas(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of CondicionIvas");
        Page<CondicionIvaDTO> page = condicionIvaService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /condicion-ivas/:id} : get the "id" condicionIva.
     *
     * @param id the id of the condicionIvaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the condicionIvaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CondicionIvaDTO> getCondicionIva(@PathVariable("id") Long id) {
        LOG.debug("REST request to get CondicionIva : {}", id);
        Optional<CondicionIvaDTO> condicionIvaDTO = condicionIvaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(condicionIvaDTO);
    }

    /**
     * {@code DELETE  /condicion-ivas/:id} : delete the "id" condicionIva.
     *
     * @param id the id of the condicionIvaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCondicionIva(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete CondicionIva : {}", id);
        condicionIvaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
