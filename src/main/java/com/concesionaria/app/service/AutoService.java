package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.AutoDTO;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.Auto}.
 */
public interface AutoService {
    /**
     * Save a auto.
     *
     * @param autoDTO the entity to save.
     * @return the persisted entity.
     */
    AutoDTO save(AutoDTO autoDTO);

    /**
     * Updates a auto.
     *
     * @param autoDTO the entity to update.
     * @return the persisted entity.
     */
    AutoDTO update(AutoDTO autoDTO);

    /**
     * Partially updates a auto.
     *
     * @param autoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AutoDTO> partialUpdate(AutoDTO autoDTO);

    /**
     * Get the "id" auto.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AutoDTO> findOne(Long id);

    /**
     * Delete the "id" auto.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
