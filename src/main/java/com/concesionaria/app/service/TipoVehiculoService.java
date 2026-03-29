package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.TipoVehiculoDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.TipoVehiculo}.
 */
public interface TipoVehiculoService {
    /**
     * Save a tipoVehiculo.
     *
     * @param tipoVehiculoDTO the entity to save.
     * @return the persisted entity.
     */
    TipoVehiculoDTO save(TipoVehiculoDTO tipoVehiculoDTO);

    /**
     * Updates a tipoVehiculo.
     *
     * @param tipoVehiculoDTO the entity to update.
     * @return the persisted entity.
     */
    TipoVehiculoDTO update(TipoVehiculoDTO tipoVehiculoDTO);

    /**
     * Partially updates a tipoVehiculo.
     *
     * @param tipoVehiculoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TipoVehiculoDTO> partialUpdate(TipoVehiculoDTO tipoVehiculoDTO);

    /**
     * Get all the tipoVehiculos.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TipoVehiculoDTO> findAll(Pageable pageable);

    /**
     * Get the "id" tipoVehiculo.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TipoVehiculoDTO> findOne(Long id);

    /**
     * Delete the "id" tipoVehiculo.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
