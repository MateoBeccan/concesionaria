package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.MarcaDTO;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.Marca}.
 */
public interface MarcaService {
    /**
     * Save a marca.
     *
     * @param marcaDTO the entity to save.
     * @return the persisted entity.
     */
    MarcaDTO save(MarcaDTO marcaDTO);

    /**
     * Updates a marca.
     *
     * @param marcaDTO the entity to update.
     * @return the persisted entity.
     */
    MarcaDTO update(MarcaDTO marcaDTO);

    /**
     * Partially updates a marca.
     *
     * @param marcaDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<MarcaDTO> partialUpdate(MarcaDTO marcaDTO);

    /**
     * Get the "id" marca.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<MarcaDTO> findOne(Long id);

    /**
     * Delete the "id" marca.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
