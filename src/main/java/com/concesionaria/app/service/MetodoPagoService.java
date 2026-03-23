package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.MetodoPagoDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.MetodoPago}.
 */
public interface MetodoPagoService {
    /**
     * Save a metodoPago.
     *
     * @param metodoPagoDTO the entity to save.
     * @return the persisted entity.
     */
    MetodoPagoDTO save(MetodoPagoDTO metodoPagoDTO);

    /**
     * Updates a metodoPago.
     *
     * @param metodoPagoDTO the entity to update.
     * @return the persisted entity.
     */
    MetodoPagoDTO update(MetodoPagoDTO metodoPagoDTO);

    /**
     * Partially updates a metodoPago.
     *
     * @param metodoPagoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<MetodoPagoDTO> partialUpdate(MetodoPagoDTO metodoPagoDTO);

    /**
     * Get all the metodoPagos.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<MetodoPagoDTO> findAll(Pageable pageable);

    /**
     * Get the "id" metodoPago.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<MetodoPagoDTO> findOne(Long id);

    /**
     * Delete the "id" metodoPago.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
