package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.service.MonedaService;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.mapper.MonedaMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Moneda}.
 */
@Service
@Transactional
public class MonedaServiceImpl implements MonedaService {

    private static final Logger LOG = LoggerFactory.getLogger(MonedaServiceImpl.class);

    private final MonedaRepository monedaRepository;

    private final MonedaMapper monedaMapper;

    public MonedaServiceImpl(MonedaRepository monedaRepository, MonedaMapper monedaMapper) {
        this.monedaRepository = monedaRepository;
        this.monedaMapper = monedaMapper;
    }

    @Override
    public MonedaDTO save(MonedaDTO monedaDTO) {
        LOG.debug("Request to save Moneda : {}", monedaDTO);
        Moneda moneda = monedaMapper.toEntity(monedaDTO);
        moneda = monedaRepository.save(moneda);
        return monedaMapper.toDto(moneda);
    }

    @Override
    public MonedaDTO update(MonedaDTO monedaDTO) {
        LOG.debug("Request to update Moneda : {}", monedaDTO);
        Moneda moneda = monedaMapper.toEntity(monedaDTO);
        moneda = monedaRepository.save(moneda);
        return monedaMapper.toDto(moneda);
    }

    @Override
    public Optional<MonedaDTO> partialUpdate(MonedaDTO monedaDTO) {
        LOG.debug("Request to partially update Moneda : {}", monedaDTO);

        return monedaRepository
            .findById(monedaDTO.getId())
            .map(existingMoneda -> {
                monedaMapper.partialUpdate(existingMoneda, monedaDTO);

                return existingMoneda;
            })
            .map(monedaRepository::save)
            .map(monedaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MonedaDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Monedas");
        return monedaRepository.findAll(pageable).map(monedaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MonedaDTO> findOne(Long id) {
        LOG.debug("Request to get Moneda : {}", id);
        return monedaRepository.findById(id).map(monedaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Moneda : {}", id);
        monedaRepository.deleteById(id);
    }
}
