package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.service.ComprobanteService;
import com.concesionaria.app.service.dto.ComprobanteDTO;
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
 * REST controller for managing {@link com.concesionaria.app.domain.Comprobante}.
 */
@RestController
@RequestMapping("/api/comprobantes")
public class ComprobanteResource {

    private static final Logger LOG = LoggerFactory.getLogger(ComprobanteResource.class);

    private static final String ENTITY_NAME = "comprobante";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final ComprobanteService comprobanteService;

    private final ComprobanteRepository comprobanteRepository;

    public ComprobanteResource(ComprobanteService comprobanteService, ComprobanteRepository comprobanteRepository) {
        this.comprobanteService = comprobanteService;
        this.comprobanteRepository = comprobanteRepository;
    }

    /**
     * {@code POST  /comprobantes} : Create a new comprobante.
     *
     * @param comprobanteDTO the comprobanteDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new comprobanteDTO, or with status {@code 400 (Bad Request)} if the comprobante has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ComprobanteDTO> createComprobante(@Valid @RequestBody ComprobanteDTO comprobanteDTO) throws URISyntaxException {
        LOG.debug("REST request to save Comprobante : {}", comprobanteDTO);
        if (comprobanteDTO.getId() != null) {
            throw new BadRequestAlertException("A new comprobante cannot already have an ID", ENTITY_NAME, "idexists");
        }
        comprobanteDTO = comprobanteService.save(comprobanteDTO);
        return ResponseEntity.created(new URI("/api/comprobantes/" + comprobanteDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, comprobanteDTO.getId().toString()))
            .body(comprobanteDTO);
    }

    /**
     * {@code PUT  /comprobantes/:id} : Updates an existing comprobante.
     *
     * @param id the id of the comprobanteDTO to save.
     * @param comprobanteDTO the comprobanteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated comprobanteDTO,
     * or with status {@code 400 (Bad Request)} if the comprobanteDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the comprobanteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ComprobanteDTO> updateComprobante(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ComprobanteDTO comprobanteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Comprobante : {}, {}", id, comprobanteDTO);
        if (comprobanteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, comprobanteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!comprobanteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        comprobanteDTO = comprobanteService.update(comprobanteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, comprobanteDTO.getId().toString()))
            .body(comprobanteDTO);
    }

    /**
     * {@code PATCH  /comprobantes/:id} : Partial updates given fields of an existing comprobante, field will ignore if it is null
     *
     * @param id the id of the comprobanteDTO to save.
     * @param comprobanteDTO the comprobanteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated comprobanteDTO,
     * or with status {@code 400 (Bad Request)} if the comprobanteDTO is not valid,
     * or with status {@code 404 (Not Found)} if the comprobanteDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the comprobanteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ComprobanteDTO> partialUpdateComprobante(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ComprobanteDTO comprobanteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Comprobante partially : {}, {}", id, comprobanteDTO);
        if (comprobanteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, comprobanteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!comprobanteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ComprobanteDTO> result = comprobanteService.partialUpdate(comprobanteDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, comprobanteDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /comprobantes} : get all the Comprobantes.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Comprobantes in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ComprobanteDTO>> getAllComprobantes(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Comprobantes");
        Page<ComprobanteDTO> page = comprobanteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /comprobantes/:id} : get the "id" comprobante.
     *
     * @param id the id of the comprobanteDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the comprobanteDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComprobanteDTO> getComprobante(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Comprobante : {}", id);
        Optional<ComprobanteDTO> comprobanteDTO = comprobanteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(comprobanteDTO);
    }

    @GetMapping("/by-venta/{ventaId}")
    public ResponseEntity<List<ComprobanteDTO>> getComprobantesByVenta(@PathVariable("ventaId") Long ventaId) {
        LOG.debug("REST request to get Comprobantes by Venta : {}", ventaId);
        return ResponseEntity.ok(comprobanteService.findByVentaId(ventaId));
    }

    /**
     * {@code DELETE  /comprobantes/:id} : delete the "id" comprobante.
     *
     * @param id the id of the comprobanteDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComprobante(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Comprobante : {}", id);
        comprobanteService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
