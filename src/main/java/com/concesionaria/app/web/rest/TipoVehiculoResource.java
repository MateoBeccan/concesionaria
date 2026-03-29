package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.TipoVehiculoRepository;
import com.concesionaria.app.service.TipoVehiculoService;
import com.concesionaria.app.service.dto.TipoVehiculoDTO;
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
 * REST controller for managing {@link com.concesionaria.app.domain.TipoVehiculo}.
 */
@RestController
@RequestMapping("/api/tipo-vehiculos")
public class TipoVehiculoResource {

    private static final Logger LOG = LoggerFactory.getLogger(TipoVehiculoResource.class);

    private static final String ENTITY_NAME = "tipoVehiculo";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final TipoVehiculoService tipoVehiculoService;

    private final TipoVehiculoRepository tipoVehiculoRepository;

    public TipoVehiculoResource(TipoVehiculoService tipoVehiculoService, TipoVehiculoRepository tipoVehiculoRepository) {
        this.tipoVehiculoService = tipoVehiculoService;
        this.tipoVehiculoRepository = tipoVehiculoRepository;
    }

    /**
     * {@code POST  /tipo-vehiculos} : Create a new tipoVehiculo.
     *
     * @param tipoVehiculoDTO the tipoVehiculoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new tipoVehiculoDTO, or with status {@code 400 (Bad Request)} if the tipoVehiculo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TipoVehiculoDTO> createTipoVehiculo(@Valid @RequestBody TipoVehiculoDTO tipoVehiculoDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save TipoVehiculo : {}", tipoVehiculoDTO);
        if (tipoVehiculoDTO.getId() != null) {
            throw new BadRequestAlertException("A new tipoVehiculo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        tipoVehiculoDTO = tipoVehiculoService.save(tipoVehiculoDTO);
        return ResponseEntity.created(new URI("/api/tipo-vehiculos/" + tipoVehiculoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, tipoVehiculoDTO.getId().toString()))
            .body(tipoVehiculoDTO);
    }

    /**
     * {@code PUT  /tipo-vehiculos/:id} : Updates an existing tipoVehiculo.
     *
     * @param id the id of the tipoVehiculoDTO to save.
     * @param tipoVehiculoDTO the tipoVehiculoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated tipoVehiculoDTO,
     * or with status {@code 400 (Bad Request)} if the tipoVehiculoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the tipoVehiculoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TipoVehiculoDTO> updateTipoVehiculo(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TipoVehiculoDTO tipoVehiculoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TipoVehiculo : {}, {}", id, tipoVehiculoDTO);
        if (tipoVehiculoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, tipoVehiculoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!tipoVehiculoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        tipoVehiculoDTO = tipoVehiculoService.update(tipoVehiculoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, tipoVehiculoDTO.getId().toString()))
            .body(tipoVehiculoDTO);
    }

    /**
     * {@code PATCH  /tipo-vehiculos/:id} : Partial updates given fields of an existing tipoVehiculo, field will ignore if it is null
     *
     * @param id the id of the tipoVehiculoDTO to save.
     * @param tipoVehiculoDTO the tipoVehiculoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated tipoVehiculoDTO,
     * or with status {@code 400 (Bad Request)} if the tipoVehiculoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the tipoVehiculoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the tipoVehiculoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TipoVehiculoDTO> partialUpdateTipoVehiculo(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TipoVehiculoDTO tipoVehiculoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TipoVehiculo partially : {}, {}", id, tipoVehiculoDTO);
        if (tipoVehiculoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, tipoVehiculoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!tipoVehiculoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TipoVehiculoDTO> result = tipoVehiculoService.partialUpdate(tipoVehiculoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, tipoVehiculoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /tipo-vehiculos} : get all the Tipo Vehiculos.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Tipo Vehiculos in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TipoVehiculoDTO>> getAllTipoVehiculos(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of TipoVehiculos");
        Page<TipoVehiculoDTO> page = tipoVehiculoService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /tipo-vehiculos/:id} : get the "id" tipoVehiculo.
     *
     * @param id the id of the tipoVehiculoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the tipoVehiculoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TipoVehiculoDTO> getTipoVehiculo(@PathVariable("id") Long id) {
        LOG.debug("REST request to get TipoVehiculo : {}", id);
        Optional<TipoVehiculoDTO> tipoVehiculoDTO = tipoVehiculoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(tipoVehiculoDTO);
    }

    /**
     * {@code DELETE  /tipo-vehiculos/:id} : delete the "id" tipoVehiculo.
     *
     * @param id the id of the tipoVehiculoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTipoVehiculo(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete TipoVehiculo : {}", id);
        tipoVehiculoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
