package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.CondicionIvaDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.CondicionIva}.
 */
public interface CondicionIvaService {
    /**
     * Save a condicionIva.
     *
     * @param condicionIvaDTO the entity to save.
     * @return the persisted entity.
     */
    CondicionIvaDTO save(CondicionIvaDTO condicionIvaDTO);

    /**
     * Updates a condicionIva.
     *
     * @param condicionIvaDTO the entity to update.
     * @return the persisted entity.
     */
    CondicionIvaDTO update(CondicionIvaDTO condicionIvaDTO);

    /**
     * Partially updates a condicionIva.
     *
     * @param condicionIvaDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CondicionIvaDTO> partialUpdate(CondicionIvaDTO condicionIvaDTO);

    /**
     * Get all the condicionIvas.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CondicionIvaDTO> findAll(Pageable pageable);

    /**
     * Get the "id" condicionIva.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CondicionIvaDTO> findOne(Long id);

    /**
     * Delete the "id" condicionIva.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
