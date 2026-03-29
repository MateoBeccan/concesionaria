package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.TraccionDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.Traccion}.
 */
public interface TraccionService {
    /**
     * Save a traccion.
     *
     * @param traccionDTO the entity to save.
     * @return the persisted entity.
     */
    TraccionDTO save(TraccionDTO traccionDTO);

    /**
     * Updates a traccion.
     *
     * @param traccionDTO the entity to update.
     * @return the persisted entity.
     */
    TraccionDTO update(TraccionDTO traccionDTO);

    /**
     * Partially updates a traccion.
     *
     * @param traccionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TraccionDTO> partialUpdate(TraccionDTO traccionDTO);

    /**
     * Get all the traccions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TraccionDTO> findAll(Pageable pageable);

    /**
     * Get the "id" traccion.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TraccionDTO> findOne(Long id);

    /**
     * Delete the "id" traccion.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
