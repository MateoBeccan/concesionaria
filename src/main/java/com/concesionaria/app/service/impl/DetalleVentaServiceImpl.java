package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.DetalleVenta;
import com.concesionaria.app.repository.DetalleVentaRepository;
import com.concesionaria.app.service.DetalleVentaService;
import com.concesionaria.app.service.dto.DetalleVentaDTO;
import com.concesionaria.app.service.mapper.DetalleVentaMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.DetalleVenta}.
 */
@Service
@Transactional
public class DetalleVentaServiceImpl implements DetalleVentaService {

    private static final Logger LOG = LoggerFactory.getLogger(DetalleVentaServiceImpl.class);

    private final DetalleVentaRepository detalleVentaRepository;

    private final DetalleVentaMapper detalleVentaMapper;

    public DetalleVentaServiceImpl(DetalleVentaRepository detalleVentaRepository, DetalleVentaMapper detalleVentaMapper) {
        this.detalleVentaRepository = detalleVentaRepository;
        this.detalleVentaMapper = detalleVentaMapper;
    }

    @Override
    public DetalleVentaDTO save(DetalleVentaDTO detalleVentaDTO) {
        LOG.debug("Request to save DetalleVenta : {}", detalleVentaDTO);
        DetalleVenta detalleVenta = detalleVentaMapper.toEntity(detalleVentaDTO);
        detalleVenta = detalleVentaRepository.save(detalleVenta);
        return detalleVentaMapper.toDto(detalleVenta);
    }

    @Override
    public DetalleVentaDTO update(DetalleVentaDTO detalleVentaDTO) {
        LOG.debug("Request to update DetalleVenta : {}", detalleVentaDTO);
        DetalleVenta detalleVenta = detalleVentaMapper.toEntity(detalleVentaDTO);
        detalleVenta = detalleVentaRepository.save(detalleVenta);
        return detalleVentaMapper.toDto(detalleVenta);
    }

    @Override
    public Optional<DetalleVentaDTO> partialUpdate(DetalleVentaDTO detalleVentaDTO) {
        LOG.debug("Request to partially update DetalleVenta : {}", detalleVentaDTO);

        return detalleVentaRepository
            .findById(detalleVentaDTO.getId())
            .map(existingDetalleVenta -> {
                detalleVentaMapper.partialUpdate(existingDetalleVenta, detalleVentaDTO);

                return existingDetalleVenta;
            })
            .map(detalleVentaRepository::save)
            .map(detalleVentaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DetalleVentaDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all DetalleVentas");
        return detalleVentaRepository.findAll(pageable).map(detalleVentaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DetalleVentaDTO> findOne(Long id) {
        LOG.debug("Request to get DetalleVenta : {}", id);
        return detalleVentaRepository.findById(id).map(detalleVentaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete DetalleVenta : {}", id);
        detalleVentaRepository.deleteById(id);
    }
}
