package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.CotizacionDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.Cotizacion}.
 */
public interface CotizacionService {
    /**
     * Save a cotizacion.
     *
     * @param cotizacionDTO the entity to save.
     * @return the persisted entity.
     */
    CotizacionDTO save(CotizacionDTO cotizacionDTO);

    /**
     * Updates a cotizacion.
     *
     * @param cotizacionDTO the entity to update.
     * @return the persisted entity.
     */
    CotizacionDTO update(CotizacionDTO cotizacionDTO);

    /**
     * Partially updates a cotizacion.
     *
     * @param cotizacionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CotizacionDTO> partialUpdate(CotizacionDTO cotizacionDTO);

    /**
     * Get all the cotizacions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CotizacionDTO> findAll(Pageable pageable);

    /**
     * Get the "id" cotizacion.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CotizacionDTO> findOne(Long id);

    /**
     * Delete the "id" cotizacion.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
