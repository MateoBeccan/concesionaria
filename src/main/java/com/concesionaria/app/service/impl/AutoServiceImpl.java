package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Auto;
import com.concesionaria.app.repository.AutoRepository;
import com.concesionaria.app.service.AutoService;
import com.concesionaria.app.service.dto.AutoDTO;
import com.concesionaria.app.service.mapper.AutoMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Auto}.
 */
@Service
@Transactional
public class AutoServiceImpl implements AutoService {

    private static final Logger LOG = LoggerFactory.getLogger(AutoServiceImpl.class);

    private final AutoRepository autoRepository;

    private final AutoMapper autoMapper;

    public AutoServiceImpl(AutoRepository autoRepository, AutoMapper autoMapper) {
        this.autoRepository = autoRepository;
        this.autoMapper = autoMapper;
    }

    @Override
    public AutoDTO save(AutoDTO autoDTO) {
        LOG.debug("Request to save Auto : {}", autoDTO);
        Auto auto = autoMapper.toEntity(autoDTO);
        auto = autoRepository.save(auto);
        return autoMapper.toDto(auto);
    }

    @Override
    public AutoDTO update(AutoDTO autoDTO) {
        LOG.debug("Request to update Auto : {}", autoDTO);
        Auto auto = autoMapper.toEntity(autoDTO);
        auto = autoRepository.save(auto);
        return autoMapper.toDto(auto);
    }

    @Override
    public Optional<AutoDTO> partialUpdate(AutoDTO autoDTO) {
        LOG.debug("Request to partially update Auto : {}", autoDTO);

        return autoRepository
            .findById(autoDTO.getId())
            .map(existingAuto -> {
                autoMapper.partialUpdate(existingAuto, autoDTO);

                return existingAuto;
            })
            .map(autoRepository::save)
            .map(autoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AutoDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Autos");
        return autoRepository.findAll(pageable).map(autoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AutoDTO> findOne(Long id) {
        LOG.debug("Request to get Auto : {}", id);
        return autoRepository.findById(id).map(autoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Auto : {}", id);
        autoRepository.deleteById(id);
    }
}
