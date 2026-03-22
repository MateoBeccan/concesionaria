package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.MotorDTO;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.Motor}.
 */
public interface MotorService {
    /**
     * Save a motor.
     *
     * @param motorDTO the entity to save.
     * @return the persisted entity.
     */
    MotorDTO save(MotorDTO motorDTO);

    /**
     * Updates a motor.
     *
     * @param motorDTO the entity to update.
     * @return the persisted entity.
     */
    MotorDTO update(MotorDTO motorDTO);

    /**
     * Partially updates a motor.
     *
     * @param motorDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<MotorDTO> partialUpdate(MotorDTO motorDTO);

    /**
     * Get the "id" motor.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<MotorDTO> findOne(Long id);

    /**
     * Delete the "id" motor.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
