package com.concesionaria.app.web.rest;

import com.concesionaria.app.repository.UbicacionStockRepository;
import com.concesionaria.app.service.UbicacionStockService;
import com.concesionaria.app.service.dto.UbicacionStockDTO;
import com.concesionaria.app.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
@RequestMapping("/api/ubicacion-stocks")
public class UbicacionStockResource {

    private static final String ENTITY_NAME = "ubicacionStock";

    @Value("${jhipster.clientApp.name:concesionaria}")
    private String applicationName;

    private final UbicacionStockService ubicacionStockService;
    private final UbicacionStockRepository ubicacionStockRepository;

    public UbicacionStockResource(UbicacionStockService ubicacionStockService, UbicacionStockRepository ubicacionStockRepository) {
        this.ubicacionStockService = ubicacionStockService;
        this.ubicacionStockRepository = ubicacionStockRepository;
    }

    @PostMapping("")
    public ResponseEntity<UbicacionStockDTO> create(@Valid @RequestBody UbicacionStockDTO dto) throws URISyntaxException {
        if (dto.getId() != null) {
            throw new BadRequestAlertException("A new ubicacionStock cannot already have an ID", ENTITY_NAME, "idexists");
        }
        UbicacionStockDTO result = ubicacionStockService.save(dto);
        return ResponseEntity.created(new URI("/api/ubicacion-stocks/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UbicacionStockDTO> update(@PathVariable Long id, @Valid @RequestBody UbicacionStockDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dto.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!ubicacionStockRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        UbicacionStockDTO result = ubicacionStockService.update(dto);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dto.getId().toString()))
            .body(result);
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<UbicacionStockDTO> partialUpdate(@PathVariable Long id, @NotNull @RequestBody UbicacionStockDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dto.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!ubicacionStockRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        Optional<UbicacionStockDTO> result = ubicacionStockService.partialUpdate(dto);
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dto.getId().toString())
        );
    }

    @GetMapping("")
    public ResponseEntity<List<UbicacionStockDTO>> getAll(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<UbicacionStockDTO> page = ubicacionStockService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UbicacionStockDTO> getOne(@PathVariable Long id) {
        return ResponseUtil.wrapOrNotFound(ubicacionStockService.findOne(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ubicacionStockService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
