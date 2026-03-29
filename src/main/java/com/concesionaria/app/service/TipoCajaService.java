package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.TipoCajaDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.TipoCaja}.
 */
public interface TipoCajaService {
    /**
     * Save a tipoCaja.
     *
     * @param tipoCajaDTO the entity to save.
     * @return the persisted entity.
     */
    TipoCajaDTO save(TipoCajaDTO tipoCajaDTO);

    /**
     * Updates a tipoCaja.
     *
     * @param tipoCajaDTO the entity to update.
     * @return the persisted entity.
     */
    TipoCajaDTO update(TipoCajaDTO tipoCajaDTO);

    /**
     * Partially updates a tipoCaja.
     *
     * @param tipoCajaDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TipoCajaDTO> partialUpdate(TipoCajaDTO tipoCajaDTO);

    /**
     * Get all the tipoCajas.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TipoCajaDTO> findAll(Pageable pageable);

    /**
     * Get the "id" tipoCaja.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TipoCajaDTO> findOne(Long id);

    /**
     * Delete the "id" tipoCaja.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
