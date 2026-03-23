package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.EstadoVentaDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.EstadoVenta}.
 */
public interface EstadoVentaService {
    /**
     * Save a estadoVenta.
     *
     * @param estadoVentaDTO the entity to save.
     * @return the persisted entity.
     */
    EstadoVentaDTO save(EstadoVentaDTO estadoVentaDTO);

    /**
     * Updates a estadoVenta.
     *
     * @param estadoVentaDTO the entity to update.
     * @return the persisted entity.
     */
    EstadoVentaDTO update(EstadoVentaDTO estadoVentaDTO);

    /**
     * Partially updates a estadoVenta.
     *
     * @param estadoVentaDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<EstadoVentaDTO> partialUpdate(EstadoVentaDTO estadoVentaDTO);

    /**
     * Get all the estadoVentas.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<EstadoVentaDTO> findAll(Pageable pageable);

    /**
     * Get the "id" estadoVenta.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<EstadoVentaDTO> findOne(Long id);

    /**
     * Delete the "id" estadoVenta.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
