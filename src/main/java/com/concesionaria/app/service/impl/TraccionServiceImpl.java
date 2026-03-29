package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Traccion;
import com.concesionaria.app.repository.TraccionRepository;
import com.concesionaria.app.service.TraccionService;
import com.concesionaria.app.service.dto.TraccionDTO;
import com.concesionaria.app.service.mapper.TraccionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Traccion}.
 */
@Service
@Transactional
public class TraccionServiceImpl implements TraccionService {

    private static final Logger LOG = LoggerFactory.getLogger(TraccionServiceImpl.class);

    private final TraccionRepository traccionRepository;

    private final TraccionMapper traccionMapper;

    public TraccionServiceImpl(TraccionRepository traccionRepository, TraccionMapper traccionMapper) {
        this.traccionRepository = traccionRepository;
        this.traccionMapper = traccionMapper;
    }

    @Override
    public TraccionDTO save(TraccionDTO traccionDTO) {
        LOG.debug("Request to save Traccion : {}", traccionDTO);
        Traccion traccion = traccionMapper.toEntity(traccionDTO);
        traccion = traccionRepository.save(traccion);
        return traccionMapper.toDto(traccion);
    }

    @Override
    public TraccionDTO update(TraccionDTO traccionDTO) {
        LOG.debug("Request to update Traccion : {}", traccionDTO);
        Traccion traccion = traccionMapper.toEntity(traccionDTO);
        traccion = traccionRepository.save(traccion);
        return traccionMapper.toDto(traccion);
    }

    @Override
    public Optional<TraccionDTO> partialUpdate(TraccionDTO traccionDTO) {
        LOG.debug("Request to partially update Traccion : {}", traccionDTO);

        return traccionRepository
            .findById(traccionDTO.getId())
            .map(existingTraccion -> {
                traccionMapper.partialUpdate(existingTraccion, traccionDTO);

                return existingTraccion;
            })
            .map(traccionRepository::save)
            .map(traccionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TraccionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Traccions");
        return traccionRepository.findAll(pageable).map(traccionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraccionDTO> findOne(Long id) {
        LOG.debug("Request to get Traccion : {}", id);
        return traccionRepository.findById(id).map(traccionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Traccion : {}", id);
        traccionRepository.deleteById(id);
    }
}
