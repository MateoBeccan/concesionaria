package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.TipoComprobanteDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.TipoComprobante}.
 */
public interface TipoComprobanteService {
    /**
     * Save a tipoComprobante.
     *
     * @param tipoComprobanteDTO the entity to save.
     * @return the persisted entity.
     */
    TipoComprobanteDTO save(TipoComprobanteDTO tipoComprobanteDTO);

    /**
     * Updates a tipoComprobante.
     *
     * @param tipoComprobanteDTO the entity to update.
     * @return the persisted entity.
     */
    TipoComprobanteDTO update(TipoComprobanteDTO tipoComprobanteDTO);

    /**
     * Partially updates a tipoComprobante.
     *
     * @param tipoComprobanteDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TipoComprobanteDTO> partialUpdate(TipoComprobanteDTO tipoComprobanteDTO);

    /**
     * Get all the tipoComprobantes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TipoComprobanteDTO> findAll(Pageable pageable);

    /**
     * Get the "id" tipoComprobante.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TipoComprobanteDTO> findOne(Long id);

    /**
     * Delete the "id" tipoComprobante.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
