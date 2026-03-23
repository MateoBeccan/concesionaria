package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.EstadoVentaRepository;
import com.concesionaria.app.service.EstadoVentaService;
import com.concesionaria.app.service.dto.EstadoVentaDTO;
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
 * REST controller for managing {@link com.concesionaria.app.domain.EstadoVenta}.
 */
@RestController
@RequestMapping("/api/estado-ventas")
public class EstadoVentaResource {

    private static final Logger LOG = LoggerFactory.getLogger(EstadoVentaResource.class);

    private static final String ENTITY_NAME = "estadoVenta";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final EstadoVentaService estadoVentaService;

    private final EstadoVentaRepository estadoVentaRepository;

    public EstadoVentaResource(EstadoVentaService estadoVentaService, EstadoVentaRepository estadoVentaRepository) {
        this.estadoVentaService = estadoVentaService;
        this.estadoVentaRepository = estadoVentaRepository;
    }

    /**
     * {@code POST  /estado-ventas} : Create a new estadoVenta.
     *
     * @param estadoVentaDTO the estadoVentaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new estadoVentaDTO, or with status {@code 400 (Bad Request)} if the estadoVenta has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EstadoVentaDTO> createEstadoVenta(@Valid @RequestBody EstadoVentaDTO estadoVentaDTO) throws URISyntaxException {
        LOG.debug("REST request to save EstadoVenta : {}", estadoVentaDTO);
        if (estadoVentaDTO.getId() != null) {
            throw new BadRequestAlertException("A new estadoVenta cannot already have an ID", ENTITY_NAME, "idexists");
        }
        estadoVentaDTO = estadoVentaService.save(estadoVentaDTO);
        return ResponseEntity.created(new URI("/api/estado-ventas/" + estadoVentaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, estadoVentaDTO.getId().toString()))
            .body(estadoVentaDTO);
    }

    /**
     * {@code PUT  /estado-ventas/:id} : Updates an existing estadoVenta.
     *
     * @param id the id of the estadoVentaDTO to save.
     * @param estadoVentaDTO the estadoVentaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated estadoVentaDTO,
     * or with status {@code 400 (Bad Request)} if the estadoVentaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the estadoVentaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EstadoVentaDTO> updateEstadoVenta(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EstadoVentaDTO estadoVentaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update EstadoVenta : {}, {}", id, estadoVentaDTO);
        if (estadoVentaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, estadoVentaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!estadoVentaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        estadoVentaDTO = estadoVentaService.update(estadoVentaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, estadoVentaDTO.getId().toString()))
            .body(estadoVentaDTO);
    }

    /**
     * {@code PATCH  /estado-ventas/:id} : Partial updates given fields of an existing estadoVenta, field will ignore if it is null
     *
     * @param id the id of the estadoVentaDTO to save.
     * @param estadoVentaDTO the estadoVentaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated estadoVentaDTO,
     * or with status {@code 400 (Bad Request)} if the estadoVentaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the estadoVentaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the estadoVentaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EstadoVentaDTO> partialUpdateEstadoVenta(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EstadoVentaDTO estadoVentaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update EstadoVenta partially : {}, {}", id, estadoVentaDTO);
        if (estadoVentaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, estadoVentaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!estadoVentaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EstadoVentaDTO> result = estadoVentaService.partialUpdate(estadoVentaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, estadoVentaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /estado-ventas} : get all the Estado Ventas.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Estado Ventas in body.
     */
    @GetMapping("")
    public ResponseEntity<List<EstadoVentaDTO>> getAllEstadoVentas(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of EstadoVentas");
        Page<EstadoVentaDTO> page = estadoVentaService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /estado-ventas/:id} : get the "id" estadoVenta.
     *
     * @param id the id of the estadoVentaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the estadoVentaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EstadoVentaDTO> getEstadoVenta(@PathVariable("id") Long id) {
        LOG.debug("REST request to get EstadoVenta : {}", id);
        Optional<EstadoVentaDTO> estadoVentaDTO = estadoVentaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(estadoVentaDTO);
    }

    /**
     * {@code DELETE  /estado-ventas/:id} : delete the "id" estadoVenta.
     *
     * @param id the id of the estadoVentaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEstadoVenta(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete EstadoVenta : {}", id);
        estadoVentaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
