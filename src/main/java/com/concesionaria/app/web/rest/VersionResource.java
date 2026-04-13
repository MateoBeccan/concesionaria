package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.VersionRepository;
import com.concesionaria.app.service.VersionService;
import com.concesionaria.app.service.dto.MotorDTO;
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

@RestController
@RequestMapping("/api/versions")
public class VersionResource {

    private static final Logger LOG = LoggerFactory.getLogger(VersionResource.class);
    private static final String ENTITY_NAME = "version";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final VersionService versionService;
    private final VersionRepository versionRepository;

    public VersionResource(VersionService versionService, VersionRepository versionRepository) {
        this.versionService = versionService;
        this.versionRepository = versionRepository;
    }

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

    @GetMapping("")
    public ResponseEntity<List<VersionDTO>> getAllVersions(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Versions");
        Page<VersionDTO> page = versionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VersionDTO> getVersion(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Version : {}", id);
        Optional<VersionDTO> versionDTO = versionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(versionDTO);
    }

    @GetMapping("/{id}/motors")
    public ResponseEntity<List<MotorDTO>> getMotorsByVersion(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Motors for Version : {}", id);
        return ResponseEntity.ok(versionService.findMotorsByVersionId(id));
    }

    @PostMapping("/{id}/motors/{motorId}")
    public ResponseEntity<List<MotorDTO>> addMotorToVersion(@PathVariable("id") Long id, @PathVariable("motorId") Long motorId) {
        LOG.debug("REST request to add Motor {} to Version {}", motorId, id);
        return ResponseEntity.ok(versionService.addMotorCompatibility(id, motorId));
    }

    @DeleteMapping("/{id}/motors/{motorId}")
    public ResponseEntity<List<MotorDTO>> removeMotorFromVersion(@PathVariable("id") Long id, @PathVariable("motorId") Long motorId) {
        LOG.debug("REST request to remove Motor {} from Version {}", motorId, id);
        return ResponseEntity.ok(versionService.removeMotorCompatibility(id, motorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVersion(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Version : {}", id);
        versionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
