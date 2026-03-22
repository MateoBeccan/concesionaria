package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.ModeloDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.Modelo}.
 */
public interface ModeloService {
    /**
     * Save a modelo.
     *
     * @param modeloDTO the entity to save.
     * @return the persisted entity.
     */
    ModeloDTO save(ModeloDTO modeloDTO);

    /**
     * Updates a modelo.
     *
     * @param modeloDTO the entity to update.
     * @return the persisted entity.
     */
    ModeloDTO update(ModeloDTO modeloDTO);

    /**
     * Partially updates a modelo.
     *
     * @param modeloDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ModeloDTO> partialUpdate(ModeloDTO modeloDTO);

    /**
     * Get all the modelos with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ModeloDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" modelo.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ModeloDTO> findOne(Long id);

    /**
     * Delete the "id" modelo.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
