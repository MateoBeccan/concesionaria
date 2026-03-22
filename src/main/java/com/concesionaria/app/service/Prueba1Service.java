package com.concesionaria.app.service;

import com.concesionaria.app.domain.Prueba1;
import com.concesionaria.app.repository.Prueba1Repository;
import com.concesionaria.app.service.dto.Prueba1DTO;
import com.concesionaria.app.service.mapper.Prueba1Mapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Prueba1}.
 */
@Service
@Transactional
public class Prueba1Service {

    private static final Logger LOG = LoggerFactory.getLogger(Prueba1Service.class);

    private final Prueba1Repository prueba1Repository;

    private final Prueba1Mapper prueba1Mapper;

    public Prueba1Service(Prueba1Repository prueba1Repository, Prueba1Mapper prueba1Mapper) {
        this.prueba1Repository = prueba1Repository;
        this.prueba1Mapper = prueba1Mapper;
    }

    /**
     * Save a prueba1.
     *
     * @param prueba1DTO the entity to save.
     * @return the persisted entity.
     */
    public Prueba1DTO save(Prueba1DTO prueba1DTO) {
        LOG.debug("Request to save Prueba1 : {}", prueba1DTO);
        Prueba1 prueba1 = prueba1Mapper.toEntity(prueba1DTO);
        prueba1 = prueba1Repository.save(prueba1);
        return prueba1Mapper.toDto(prueba1);
    }

    /**
     * Get one prueba1 by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Prueba1DTO> findOne(Long id) {
        LOG.debug("Request to get Prueba1 : {}", id);
        return prueba1Repository.findById(id).map(prueba1Mapper::toDto);
    }

    /**
     * Delete the prueba1 by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Prueba1 : {}", id);
        prueba1Repository.deleteById(id);
    }
}
