package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.MonedaDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.Moneda}.
 */
public interface MonedaService {
    /**
     * Save a moneda.
     *
     * @param monedaDTO the entity to save.
     * @return the persisted entity.
     */
    MonedaDTO save(MonedaDTO monedaDTO);

    /**
     * Updates a moneda.
     *
     * @param monedaDTO the entity to update.
     * @return the persisted entity.
     */
    MonedaDTO update(MonedaDTO monedaDTO);

    /**
     * Partially updates a moneda.
     *
     * @param monedaDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<MonedaDTO> partialUpdate(MonedaDTO monedaDTO);

    /**
     * Get all the monedas.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<MonedaDTO> findAll(Pageable pageable);

    /**
     * Get the "id" moneda.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<MonedaDTO> findOne(Long id);

    /**
     * Delete the "id" moneda.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
