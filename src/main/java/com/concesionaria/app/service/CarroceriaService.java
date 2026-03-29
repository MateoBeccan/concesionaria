package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.CarroceriaDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.Carroceria}.
 */
public interface CarroceriaService {
    /**
     * Save a carroceria.
     *
     * @param carroceriaDTO the entity to save.
     * @return the persisted entity.
     */
    CarroceriaDTO save(CarroceriaDTO carroceriaDTO);

    /**
     * Updates a carroceria.
     *
     * @param carroceriaDTO the entity to update.
     * @return the persisted entity.
     */
    CarroceriaDTO update(CarroceriaDTO carroceriaDTO);

    /**
     * Partially updates a carroceria.
     *
     * @param carroceriaDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CarroceriaDTO> partialUpdate(CarroceriaDTO carroceriaDTO);

    /**
     * Get all the carrocerias.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CarroceriaDTO> findAll(Pageable pageable);

    /**
     * Get the "id" carroceria.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CarroceriaDTO> findOne(Long id);

    /**
     * Delete the "id" carroceria.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
