package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.EstadoVenta;
import com.concesionaria.app.repository.EstadoVentaRepository;
import com.concesionaria.app.service.EstadoVentaService;
import com.concesionaria.app.service.dto.EstadoVentaDTO;
import com.concesionaria.app.service.mapper.EstadoVentaMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.EstadoVenta}.
 */
@Service
@Transactional
public class EstadoVentaServiceImpl implements EstadoVentaService {

    private static final Logger LOG = LoggerFactory.getLogger(EstadoVentaServiceImpl.class);

    private final EstadoVentaRepository estadoVentaRepository;

    private final EstadoVentaMapper estadoVentaMapper;

    public EstadoVentaServiceImpl(EstadoVentaRepository estadoVentaRepository, EstadoVentaMapper estadoVentaMapper) {
        this.estadoVentaRepository = estadoVentaRepository;
        this.estadoVentaMapper = estadoVentaMapper;
    }

    @Override
    public EstadoVentaDTO save(EstadoVentaDTO estadoVentaDTO) {
        LOG.debug("Request to save EstadoVenta : {}", estadoVentaDTO);
        EstadoVenta estadoVenta = estadoVentaMapper.toEntity(estadoVentaDTO);
        estadoVenta = estadoVentaRepository.save(estadoVenta);
        return estadoVentaMapper.toDto(estadoVenta);
    }

    @Override
    public EstadoVentaDTO update(EstadoVentaDTO estadoVentaDTO) {
        LOG.debug("Request to update EstadoVenta : {}", estadoVentaDTO);
        EstadoVenta estadoVenta = estadoVentaMapper.toEntity(estadoVentaDTO);
        estadoVenta = estadoVentaRepository.save(estadoVenta);
        return estadoVentaMapper.toDto(estadoVenta);
    }

    @Override
    public Optional<EstadoVentaDTO> partialUpdate(EstadoVentaDTO estadoVentaDTO) {
        LOG.debug("Request to partially update EstadoVenta : {}", estadoVentaDTO);

        return estadoVentaRepository
            .findById(estadoVentaDTO.getId())
            .map(existingEstadoVenta -> {
                estadoVentaMapper.partialUpdate(existingEstadoVenta, estadoVentaDTO);

                return existingEstadoVenta;
            })
            .map(estadoVentaRepository::save)
            .map(estadoVentaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EstadoVentaDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all EstadoVentas");
        return estadoVentaRepository.findAll(pageable).map(estadoVentaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EstadoVentaDTO> findOne(Long id) {
        LOG.debug("Request to get EstadoVenta : {}", id);
        return estadoVentaRepository.findById(id).map(estadoVentaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete EstadoVenta : {}", id);
        estadoVentaRepository.deleteById(id);
    }
}
