    package com.concesionaria.app.service;

    import com.concesionaria.app.service.dto.VehiculoDTO;
    import java.util.List;
    import java.util.Optional;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;

    /**
     * Service Interface for managing {@link com.concesionaria.app.domain.Vehiculo}.
     */
    public interface   VehiculoService {
        /**
         * Save a vehiculo.
         *
         * @param vehiculoDTO the entity to save.
         * @return the persisted entity.
         */
        VehiculoDTO save(VehiculoDTO vehiculoDTO);

        /**
         * Updates a vehiculo.
         *
         * @param vehiculoDTO the entity to update.
         * @return the persisted entity.
         */
        VehiculoDTO update(VehiculoDTO vehiculoDTO);

        /**
         * Partially updates a vehiculo.
         *
         * @param vehiculoDTO the entity to update partially.
         * @return the persisted entity.
         */
        Optional<VehiculoDTO> partialUpdate(VehiculoDTO vehiculoDTO);

        /**
         * Get all the vehiculos.
         *
         * @param pageable the pagination information.
         * @return the list of entities.
         */
        Page<VehiculoDTO> findAll(Pageable pageable);

        /**
         * Get all the VehiculoDTO where Inventario is {@code null}.
         *
         * @return the {@link List} of entities.
         */
        List<VehiculoDTO> findAllWhereInventarioIsNull();

        /**
         * Get the "id" vehiculo.
         *
         * @param id the id of the entity.
         * @return the entity.
         */
        Optional<VehiculoDTO> findOne(Long id);

        /**
         * Delete the "id" vehiculo.
         *
         * @param id the id of the entity.
         */
        void delete(Long id);

        void reservarVehiculo(Long vehiculoId, Long clienteId);

        void venderVehiculo(Long vehiculoId, Long clienteId);

        void cancelarReserva(Long vehiculoId);

        Optional<VehiculoDTO> findByPatente(String patente);

    }
