package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.TipoDocumentoDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.TipoDocumento}.
 */
public interface TipoDocumentoService {
    /**
     * Save a tipoDocumento.
     *
     * @param tipoDocumentoDTO the entity to save.
     * @return the persisted entity.
     */
    TipoDocumentoDTO save(TipoDocumentoDTO tipoDocumentoDTO);

    /**
     * Updates a tipoDocumento.
     *
     * @param tipoDocumentoDTO the entity to update.
     * @return the persisted entity.
     */
    TipoDocumentoDTO update(TipoDocumentoDTO tipoDocumentoDTO);

    /**
     * Partially updates a tipoDocumento.
     *
     * @param tipoDocumentoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TipoDocumentoDTO> partialUpdate(TipoDocumentoDTO tipoDocumentoDTO);

    /**
     * Get all the tipoDocumentos.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TipoDocumentoDTO> findAll(Pageable pageable);

    /**
     * Get the "id" tipoDocumento.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TipoDocumentoDTO> findOne(Long id);

    /**
     * Delete the "id" tipoDocumento.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
