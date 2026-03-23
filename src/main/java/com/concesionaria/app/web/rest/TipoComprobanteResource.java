package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.TipoComprobanteRepository;
import com.concesionaria.app.service.TipoComprobanteService;
import com.concesionaria.app.service.dto.TipoComprobanteDTO;
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
 * REST controller for managing {@link com.concesionaria.app.domain.TipoComprobante}.
 */
@RestController
@RequestMapping("/api/tipo-comprobantes")
public class TipoComprobanteResource {

    private static final Logger LOG = LoggerFactory.getLogger(TipoComprobanteResource.class);

    private static final String ENTITY_NAME = "tipoComprobante";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final TipoComprobanteService tipoComprobanteService;

    private final TipoComprobanteRepository tipoComprobanteRepository;

    public TipoComprobanteResource(TipoComprobanteService tipoComprobanteService, TipoComprobanteRepository tipoComprobanteRepository) {
        this.tipoComprobanteService = tipoComprobanteService;
        this.tipoComprobanteRepository = tipoComprobanteRepository;
    }

    /**
     * {@code POST  /tipo-comprobantes} : Create a new tipoComprobante.
     *
     * @param tipoComprobanteDTO the tipoComprobanteDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new tipoComprobanteDTO, or with status {@code 400 (Bad Request)} if the tipoComprobante has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TipoComprobanteDTO> createTipoComprobante(@Valid @RequestBody TipoComprobanteDTO tipoComprobanteDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save TipoComprobante : {}", tipoComprobanteDTO);
        if (tipoComprobanteDTO.getId() != null) {
            throw new BadRequestAlertException("A new tipoComprobante cannot already have an ID", ENTITY_NAME, "idexists");
        }
        tipoComprobanteDTO = tipoComprobanteService.save(tipoComprobanteDTO);
        return ResponseEntity.created(new URI("/api/tipo-comprobantes/" + tipoComprobanteDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, tipoComprobanteDTO.getId().toString()))
            .body(tipoComprobanteDTO);
    }

    /**
     * {@code PUT  /tipo-comprobantes/:id} : Updates an existing tipoComprobante.
     *
     * @param id the id of the tipoComprobanteDTO to save.
     * @param tipoComprobanteDTO the tipoComprobanteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated tipoComprobanteDTO,
     * or with status {@code 400 (Bad Request)} if the tipoComprobanteDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the tipoComprobanteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TipoComprobanteDTO> updateTipoComprobante(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TipoComprobanteDTO tipoComprobanteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TipoComprobante : {}, {}", id, tipoComprobanteDTO);
        if (tipoComprobanteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, tipoComprobanteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!tipoComprobanteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        tipoComprobanteDTO = tipoComprobanteService.update(tipoComprobanteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, tipoComprobanteDTO.getId().toString()))
            .body(tipoComprobanteDTO);
    }

    /**
     * {@code PATCH  /tipo-comprobantes/:id} : Partial updates given fields of an existing tipoComprobante, field will ignore if it is null
     *
     * @param id the id of the tipoComprobanteDTO to save.
     * @param tipoComprobanteDTO the tipoComprobanteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated tipoComprobanteDTO,
     * or with status {@code 400 (Bad Request)} if the tipoComprobanteDTO is not valid,
     * or with status {@code 404 (Not Found)} if the tipoComprobanteDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the tipoComprobanteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TipoComprobanteDTO> partialUpdateTipoComprobante(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TipoComprobanteDTO tipoComprobanteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TipoComprobante partially : {}, {}", id, tipoComprobanteDTO);
        if (tipoComprobanteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, tipoComprobanteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!tipoComprobanteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TipoComprobanteDTO> result = tipoComprobanteService.partialUpdate(tipoComprobanteDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, tipoComprobanteDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /tipo-comprobantes} : get all the Tipo Comprobantes.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Tipo Comprobantes in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TipoComprobanteDTO>> getAllTipoComprobantes(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of TipoComprobantes");
        Page<TipoComprobanteDTO> page = tipoComprobanteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /tipo-comprobantes/:id} : get the "id" tipoComprobante.
     *
     * @param id the id of the tipoComprobanteDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the tipoComprobanteDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TipoComprobanteDTO> getTipoComprobante(@PathVariable("id") Long id) {
        LOG.debug("REST request to get TipoComprobante : {}", id);
        Optional<TipoComprobanteDTO> tipoComprobanteDTO = tipoComprobanteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(tipoComprobanteDTO);
    }

    /**
     * {@code DELETE  /tipo-comprobantes/:id} : delete the "id" tipoComprobante.
     *
     * @param id the id of the tipoComprobanteDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTipoComprobante(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete TipoComprobante : {}", id);
        tipoComprobanteService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
