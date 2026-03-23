package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.repository.TipoComprobanteRepository;
import com.concesionaria.app.service.TipoComprobanteService;
import com.concesionaria.app.service.dto.TipoComprobanteDTO;
import com.concesionaria.app.service.mapper.TipoComprobanteMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.TipoComprobante}.
 */
@Service
@Transactional
public class TipoComprobanteServiceImpl implements TipoComprobanteService {

    private static final Logger LOG = LoggerFactory.getLogger(TipoComprobanteServiceImpl.class);

    private final TipoComprobanteRepository tipoComprobanteRepository;

    private final TipoComprobanteMapper tipoComprobanteMapper;

    public TipoComprobanteServiceImpl(TipoComprobanteRepository tipoComprobanteRepository, TipoComprobanteMapper tipoComprobanteMapper) {
        this.tipoComprobanteRepository = tipoComprobanteRepository;
        this.tipoComprobanteMapper = tipoComprobanteMapper;
    }

    @Override
    public TipoComprobanteDTO save(TipoComprobanteDTO tipoComprobanteDTO) {
        LOG.debug("Request to save TipoComprobante : {}", tipoComprobanteDTO);
        TipoComprobante tipoComprobante = tipoComprobanteMapper.toEntity(tipoComprobanteDTO);
        tipoComprobante = tipoComprobanteRepository.save(tipoComprobante);
        return tipoComprobanteMapper.toDto(tipoComprobante);
    }

    @Override
    public TipoComprobanteDTO update(TipoComprobanteDTO tipoComprobanteDTO) {
        LOG.debug("Request to update TipoComprobante : {}", tipoComprobanteDTO);
        TipoComprobante tipoComprobante = tipoComprobanteMapper.toEntity(tipoComprobanteDTO);
        tipoComprobante = tipoComprobanteRepository.save(tipoComprobante);
        return tipoComprobanteMapper.toDto(tipoComprobante);
    }

    @Override
    public Optional<TipoComprobanteDTO> partialUpdate(TipoComprobanteDTO tipoComprobanteDTO) {
        LOG.debug("Request to partially update TipoComprobante : {}", tipoComprobanteDTO);

        return tipoComprobanteRepository
            .findById(tipoComprobanteDTO.getId())
            .map(existingTipoComprobante -> {
                tipoComprobanteMapper.partialUpdate(existingTipoComprobante, tipoComprobanteDTO);

                return existingTipoComprobante;
            })
            .map(tipoComprobanteRepository::save)
            .map(tipoComprobanteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TipoComprobanteDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all TipoComprobantes");
        return tipoComprobanteRepository.findAll(pageable).map(tipoComprobanteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoComprobanteDTO> findOne(Long id) {
        LOG.debug("Request to get TipoComprobante : {}", id);
        return tipoComprobanteRepository.findById(id).map(tipoComprobanteMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete TipoComprobante : {}", id);
        tipoComprobanteRepository.deleteById(id);
    }
}
