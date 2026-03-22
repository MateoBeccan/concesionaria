package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.MotorRepository;
import com.concesionaria.app.service.MotorQueryService;
import com.concesionaria.app.service.MotorService;
import com.concesionaria.app.service.criteria.MotorCriteria;
import com.concesionaria.app.service.dto.MotorDTO;
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
 * REST controller for managing {@link com.concesionaria.app.domain.Motor}.
 */
@RestController
@RequestMapping("/api/motors")
public class MotorResource {

    private static final Logger LOG = LoggerFactory.getLogger(MotorResource.class);

    private static final String ENTITY_NAME = "motor";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final MotorService motorService;

    private final MotorRepository motorRepository;

    private final MotorQueryService motorQueryService;

    public MotorResource(MotorService motorService, MotorRepository motorRepository, MotorQueryService motorQueryService) {
        this.motorService = motorService;
        this.motorRepository = motorRepository;
        this.motorQueryService = motorQueryService;
    }

    /**
     * {@code POST  /motors} : Create a new motor.
     *
     * @param motorDTO the motorDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new motorDTO, or with status {@code 400 (Bad Request)} if the motor has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MotorDTO> createMotor(@Valid @RequestBody MotorDTO motorDTO) throws URISyntaxException {
        LOG.debug("REST request to save Motor : {}", motorDTO);
        if (motorDTO.getId() != null) {
            throw new BadRequestAlertException("A new motor cannot already have an ID", ENTITY_NAME, "idexists");
        }
        motorDTO = motorService.save(motorDTO);
        return ResponseEntity.created(new URI("/api/motors/" + motorDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, motorDTO.getId().toString()))
            .body(motorDTO);
    }

    /**
     * {@code PUT  /motors/:id} : Updates an existing motor.
     *
     * @param id the id of the motorDTO to save.
     * @param motorDTO the motorDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated motorDTO,
     * or with status {@code 400 (Bad Request)} if the motorDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the motorDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MotorDTO> updateMotor(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody MotorDTO motorDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Motor : {}, {}", id, motorDTO);
        if (motorDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, motorDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!motorRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        motorDTO = motorService.update(motorDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, motorDTO.getId().toString()))
            .body(motorDTO);
    }

    /**
     * {@code PATCH  /motors/:id} : Partial updates given fields of an existing motor, field will ignore if it is null
     *
     * @param id the id of the motorDTO to save.
     * @param motorDTO the motorDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated motorDTO,
     * or with status {@code 400 (Bad Request)} if the motorDTO is not valid,
     * or with status {@code 404 (Not Found)} if the motorDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the motorDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MotorDTO> partialUpdateMotor(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody MotorDTO motorDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Motor partially : {}, {}", id, motorDTO);
        if (motorDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, motorDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!motorRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MotorDTO> result = motorService.partialUpdate(motorDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, motorDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /motors} : get all the Motors.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Motors in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MotorDTO>> getAllMotors(
        MotorCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Motors by criteria: {}", criteria);

        Page<MotorDTO> page = motorQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /motors/count} : count all the motors.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countMotors(MotorCriteria criteria) {
        LOG.debug("REST request to count Motors by criteria: {}", criteria);
        return ResponseEntity.ok().body(motorQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /motors/:id} : get the "id" motor.
     *
     * @param id the id of the motorDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the motorDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MotorDTO> getMotor(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Motor : {}", id);
        Optional<MotorDTO> motorDTO = motorService.findOne(id);
        return ResponseUtil.wrapOrNotFound(motorDTO);
    }

    /**
     * {@code DELETE  /motors/:id} : delete the "id" motor.
     *
     * @param id the id of the motorDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMotor(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Motor : {}", id);
        motorService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
