package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.Prueba1Repository;
import com.concesionaria.app.service.Prueba1QueryService;
import com.concesionaria.app.service.Prueba1Service;
import com.concesionaria.app.service.criteria.Prueba1Criteria;
import com.concesionaria.app.service.dto.Prueba1DTO;
import com.concesionaria.app.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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
 * REST controller for managing {@link com.concesionaria.app.domain.Prueba1}.
 */
@RestController
@RequestMapping("/api/prueba-1-s")
public class Prueba1Resource {

    private static final Logger LOG = LoggerFactory.getLogger(Prueba1Resource.class);

    private static final String ENTITY_NAME = "prueba1";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final Prueba1Service prueba1Service;

    private final Prueba1Repository prueba1Repository;

    private final Prueba1QueryService prueba1QueryService;

    public Prueba1Resource(Prueba1Service prueba1Service, Prueba1Repository prueba1Repository, Prueba1QueryService prueba1QueryService) {
        this.prueba1Service = prueba1Service;
        this.prueba1Repository = prueba1Repository;
        this.prueba1QueryService = prueba1QueryService;
    }

    /**
     * {@code POST  /prueba-1-s} : Create a new prueba1.
     *
     * @param prueba1DTO the prueba1DTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new prueba1DTO, or with status {@code 400 (Bad Request)} if the prueba1 has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Prueba1DTO> createPrueba1(@RequestBody Prueba1DTO prueba1DTO) throws URISyntaxException {
        LOG.debug("REST request to save Prueba1 : {}", prueba1DTO);
        if (prueba1DTO.getId() != null) {
            throw new BadRequestAlertException("A new prueba1 cannot already have an ID", ENTITY_NAME, "idexists");
        }
        prueba1DTO = prueba1Service.save(prueba1DTO);
        return ResponseEntity.created(new URI("/api/prueba-1-s/" + prueba1DTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, prueba1DTO.getId().toString()))
            .body(prueba1DTO);
    }

    /**
     * {@code GET  /prueba-1-s} : get all the Prueba 1s.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Prueba 1s in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Prueba1DTO>> getAllPrueba1s(
        Prueba1Criteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Prueba1s by criteria: {}", criteria);

        Page<Prueba1DTO> page = prueba1QueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /prueba-1-s/count} : count all the prueba1s.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countPrueba1s(Prueba1Criteria criteria) {
        LOG.debug("REST request to count Prueba1s by criteria: {}", criteria);
        return ResponseEntity.ok().body(prueba1QueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /prueba-1-s/:id} : get the "id" prueba1.
     *
     * @param id the id of the prueba1DTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the prueba1DTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Prueba1DTO> getPrueba1(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Prueba1 : {}", id);
        Optional<Prueba1DTO> prueba1DTO = prueba1Service.findOne(id);
        return ResponseUtil.wrapOrNotFound(prueba1DTO);
    }

    /**
     * {@code DELETE  /prueba-1-s/:id} : delete the "id" prueba1.
     *
     * @param id the id of the prueba1DTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrueba1(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Prueba1 : {}", id);
        prueba1Service.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
