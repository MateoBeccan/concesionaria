package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.service.MetodoPagoService;
import com.concesionaria.app.service.dto.MetodoPagoDTO;
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
 * REST controller for managing {@link com.concesionaria.app.domain.MetodoPago}.
 */
@RestController
@RequestMapping("/api/metodo-pagos")
public class MetodoPagoResource {

    private static final Logger LOG = LoggerFactory.getLogger(MetodoPagoResource.class);

    private static final String ENTITY_NAME = "metodoPago";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final MetodoPagoService metodoPagoService;

    private final MetodoPagoRepository metodoPagoRepository;

    public MetodoPagoResource(MetodoPagoService metodoPagoService, MetodoPagoRepository metodoPagoRepository) {
        this.metodoPagoService = metodoPagoService;
        this.metodoPagoRepository = metodoPagoRepository;
    }

    /**
     * {@code POST  /metodo-pagos} : Create a new metodoPago.
     *
     * @param metodoPagoDTO the metodoPagoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new metodoPagoDTO, or with status {@code 400 (Bad Request)} if the metodoPago has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MetodoPagoDTO> createMetodoPago(@Valid @RequestBody MetodoPagoDTO metodoPagoDTO) throws URISyntaxException {
        LOG.debug("REST request to save MetodoPago : {}", metodoPagoDTO);
        if (metodoPagoDTO.getId() != null) {
            throw new BadRequestAlertException("A new metodoPago cannot already have an ID", ENTITY_NAME, "idexists");
        }
        metodoPagoDTO = metodoPagoService.save(metodoPagoDTO);
        return ResponseEntity.created(new URI("/api/metodo-pagos/" + metodoPagoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, metodoPagoDTO.getId().toString()))
            .body(metodoPagoDTO);
    }

    /**
     * {@code PUT  /metodo-pagos/:id} : Updates an existing metodoPago.
     *
     * @param id the id of the metodoPagoDTO to save.
     * @param metodoPagoDTO the metodoPagoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated metodoPagoDTO,
     * or with status {@code 400 (Bad Request)} if the metodoPagoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the metodoPagoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MetodoPagoDTO> updateMetodoPago(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody MetodoPagoDTO metodoPagoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update MetodoPago : {}, {}", id, metodoPagoDTO);
        if (metodoPagoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, metodoPagoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!metodoPagoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        metodoPagoDTO = metodoPagoService.update(metodoPagoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, metodoPagoDTO.getId().toString()))
            .body(metodoPagoDTO);
    }

    /**
     * {@code PATCH  /metodo-pagos/:id} : Partial updates given fields of an existing metodoPago, field will ignore if it is null
     *
     * @param id the id of the metodoPagoDTO to save.
     * @param metodoPagoDTO the metodoPagoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated metodoPagoDTO,
     * or with status {@code 400 (Bad Request)} if the metodoPagoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the metodoPagoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the metodoPagoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MetodoPagoDTO> partialUpdateMetodoPago(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody MetodoPagoDTO metodoPagoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MetodoPago partially : {}, {}", id, metodoPagoDTO);
        if (metodoPagoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, metodoPagoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!metodoPagoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MetodoPagoDTO> result = metodoPagoService.partialUpdate(metodoPagoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, metodoPagoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /metodo-pagos} : get all the Metodo Pagos.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Metodo Pagos in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MetodoPagoDTO>> getAllMetodoPagos(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of MetodoPagos");
        Page<MetodoPagoDTO> page = metodoPagoService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /metodo-pagos/:id} : get the "id" metodoPago.
     *
     * @param id the id of the metodoPagoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the metodoPagoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MetodoPagoDTO> getMetodoPago(@PathVariable("id") Long id) {
        LOG.debug("REST request to get MetodoPago : {}", id);
        Optional<MetodoPagoDTO> metodoPagoDTO = metodoPagoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(metodoPagoDTO);
    }

    /**
     * {@code DELETE  /metodo-pagos/:id} : delete the "id" metodoPago.
     *
     * @param id the id of the metodoPagoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetodoPago(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete MetodoPago : {}", id);
        metodoPagoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
