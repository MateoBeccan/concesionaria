package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.DetalleVentaDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.DetalleVenta}.
 */
public interface DetalleVentaService {
    /**
     * Save a detalleVenta.
     *
     * @param detalleVentaDTO the entity to save.
     * @return the persisted entity.
     */
    DetalleVentaDTO save(DetalleVentaDTO detalleVentaDTO);

    /**
     * Updates a detalleVenta.
     *
     * @param detalleVentaDTO the entity to update.
     * @return the persisted entity.
     */
    DetalleVentaDTO update(DetalleVentaDTO detalleVentaDTO);

    /**
     * Partially updates a detalleVenta.
     *
     * @param detalleVentaDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DetalleVentaDTO> partialUpdate(DetalleVentaDTO detalleVentaDTO);

    /**
     * Get all the detalleVentas.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DetalleVentaDTO> findAll(Pageable pageable);

    /**
     * Get the "id" detalleVenta.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DetalleVentaDTO> findOne(Long id);

    /**
     * Delete the "id" detalleVenta.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
