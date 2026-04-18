package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.ComprobanteDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.Comprobante}.
 */
public interface ComprobanteService {
    /**
     * Save a comprobante.
     *
     * @param comprobanteDTO the entity to save.
     * @return the persisted entity.
     */
    ComprobanteDTO save(ComprobanteDTO comprobanteDTO);

    /**
     * Updates a comprobante.
     *
     * @param comprobanteDTO the entity to update.
     * @return the persisted entity.
     */
    ComprobanteDTO update(ComprobanteDTO comprobanteDTO);

    /**
     * Partially updates a comprobante.
     *
     * @param comprobanteDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ComprobanteDTO> partialUpdate(ComprobanteDTO comprobanteDTO);

    /**
     * Get all the comprobantes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ComprobanteDTO> findAll(Pageable pageable);

    /**
     * Get the "id" comprobante.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ComprobanteDTO> findOne(Long id);

    List<ComprobanteDTO> findByVentaId(Long ventaId);

    /**
     * Delete the "id" comprobante.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
