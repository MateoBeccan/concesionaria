package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.CombustibleDTO;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.Combustible}.
 */
public interface CombustibleService {
    /**
     * Save a combustible.
     *
     * @param combustibleDTO the entity to save.
     * @return the persisted entity.
     */
    CombustibleDTO save(CombustibleDTO combustibleDTO);

    /**
     * Updates a combustible.
     *
     * @param combustibleDTO the entity to update.
     * @return the persisted entity.
     */
    CombustibleDTO update(CombustibleDTO combustibleDTO);

    /**
     * Partially updates a combustible.
     *
     * @param combustibleDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CombustibleDTO> partialUpdate(CombustibleDTO combustibleDTO);

    /**
     * Get the "id" combustible.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CombustibleDTO> findOne(Long id);

    /**
     * Delete the "id" combustible.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
