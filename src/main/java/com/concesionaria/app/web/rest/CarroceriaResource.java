package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.CarroceriaRepository;
import com.concesionaria.app.service.CarroceriaService;
import com.concesionaria.app.service.dto.CarroceriaDTO;
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
 * REST controller for managing {@link com.concesionaria.app.domain.Carroceria}.
 */
@RestController
@RequestMapping("/api/carrocerias")
public class CarroceriaResource {

    private static final Logger LOG = LoggerFactory.getLogger(CarroceriaResource.class);

    private static final String ENTITY_NAME = "carroceria";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final CarroceriaService carroceriaService;

    private final CarroceriaRepository carroceriaRepository;

    public CarroceriaResource(CarroceriaService carroceriaService, CarroceriaRepository carroceriaRepository) {
        this.carroceriaService = carroceriaService;
        this.carroceriaRepository = carroceriaRepository;
    }

    /**
     * {@code POST  /carrocerias} : Create a new carroceria.
     *
     * @param carroceriaDTO the carroceriaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new carroceriaDTO, or with status {@code 400 (Bad Request)} if the carroceria has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CarroceriaDTO> createCarroceria(@Valid @RequestBody CarroceriaDTO carroceriaDTO) throws URISyntaxException {
        LOG.debug("REST request to save Carroceria : {}", carroceriaDTO);
        if (carroceriaDTO.getId() != null) {
            throw new BadRequestAlertException("A new carroceria cannot already have an ID", ENTITY_NAME, "idexists");
        }
        carroceriaDTO = carroceriaService.save(carroceriaDTO);
        return ResponseEntity.created(new URI("/api/carrocerias/" + carroceriaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, carroceriaDTO.getId().toString()))
            .body(carroceriaDTO);
    }

    /**
     * {@code PUT  /carrocerias/:id} : Updates an existing carroceria.
     *
     * @param id the id of the carroceriaDTO to save.
     * @param carroceriaDTO the carroceriaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated carroceriaDTO,
     * or with status {@code 400 (Bad Request)} if the carroceriaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the carroceriaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CarroceriaDTO> updateCarroceria(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CarroceriaDTO carroceriaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Carroceria : {}, {}", id, carroceriaDTO);
        if (carroceriaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, carroceriaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!carroceriaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        carroceriaDTO = carroceriaService.update(carroceriaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, carroceriaDTO.getId().toString()))
            .body(carroceriaDTO);
    }

    /**
     * {@code PATCH  /carrocerias/:id} : Partial updates given fields of an existing carroceria, field will ignore if it is null
     *
     * @param id the id of the carroceriaDTO to save.
     * @param carroceriaDTO the carroceriaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated carroceriaDTO,
     * or with status {@code 400 (Bad Request)} if the carroceriaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the carroceriaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the carroceriaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CarroceriaDTO> partialUpdateCarroceria(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CarroceriaDTO carroceriaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Carroceria partially : {}, {}", id, carroceriaDTO);
        if (carroceriaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, carroceriaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!carroceriaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CarroceriaDTO> result = carroceriaService.partialUpdate(carroceriaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, carroceriaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /carrocerias} : get all the Carrocerias.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Carrocerias in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CarroceriaDTO>> getAllCarrocerias(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Carrocerias");
        Page<CarroceriaDTO> page = carroceriaService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /carrocerias/:id} : get the "id" carroceria.
     *
     * @param id the id of the carroceriaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the carroceriaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CarroceriaDTO> getCarroceria(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Carroceria : {}", id);
        Optional<CarroceriaDTO> carroceriaDTO = carroceriaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(carroceriaDTO);
    }

    /**
     * {@code DELETE  /carrocerias/:id} : delete the "id" carroceria.
     *
     * @param id the id of the carroceriaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCarroceria(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Carroceria : {}", id);
        carroceriaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
