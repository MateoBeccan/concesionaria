    package com.concesionaria.app.service;

    import com.concesionaria.app.service.dto.InventarioDTO;
    import java.util.Optional;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;

    /**
     * Service Interface for managing {@link com.concesionaria.app.domain.Inventario}.
     */
    public interface InventarioService {
        /**
         * Save a inventario.
         *
         * @param inventarioDTO the entity to save.
         * @return the persisted entity.
         */
        InventarioDTO save(InventarioDTO inventarioDTO);

        /**
         * Updates a inventario.
         *
         * @param inventarioDTO the entity to update.
         * @return the persisted entity.
         */
        InventarioDTO update(InventarioDTO inventarioDTO);

        /**
         * Partially updates a inventario.
         *
         * @param inventarioDTO the entity to update partially.
         * @return the persisted entity.
         */
        Optional<InventarioDTO> partialUpdate(InventarioDTO inventarioDTO);

        /**
         * Get all the inventarios.
         *
         * @param pageable the pagination information.
         * @return the list of entities.
         */
        Page<InventarioDTO> findAll(Pageable pageable);

        /**
         * Get the "id" inventario.
         *
         * @param id the id of the entity.
         * @return the entity.
         */
        Optional<InventarioDTO> findOne(Long id);

        /**
         * Delete the "id" inventario.
         *
         * @param id the id of the entity.
         */
        void delete(Long id);
    }
