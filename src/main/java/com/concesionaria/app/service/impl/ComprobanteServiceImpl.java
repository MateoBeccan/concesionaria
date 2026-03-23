package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.service.ComprobanteService;
import com.concesionaria.app.service.dto.ComprobanteDTO;
import com.concesionaria.app.service.mapper.ComprobanteMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Comprobante}.
 */
@Service
@Transactional
public class ComprobanteServiceImpl implements ComprobanteService {

    private static final Logger LOG = LoggerFactory.getLogger(ComprobanteServiceImpl.class);

    private final ComprobanteRepository comprobanteRepository;

    private final ComprobanteMapper comprobanteMapper;

    public ComprobanteServiceImpl(ComprobanteRepository comprobanteRepository, ComprobanteMapper comprobanteMapper) {
        this.comprobanteRepository = comprobanteRepository;
        this.comprobanteMapper = comprobanteMapper;
    }

    @Override
    public ComprobanteDTO save(ComprobanteDTO comprobanteDTO) {
        LOG.debug("Request to save Comprobante : {}", comprobanteDTO);
        Comprobante comprobante = comprobanteMapper.toEntity(comprobanteDTO);
        comprobante = comprobanteRepository.save(comprobante);
        return comprobanteMapper.toDto(comprobante);
    }

    @Override
    public ComprobanteDTO update(ComprobanteDTO comprobanteDTO) {
        LOG.debug("Request to update Comprobante : {}", comprobanteDTO);
        Comprobante comprobante = comprobanteMapper.toEntity(comprobanteDTO);
        comprobante = comprobanteRepository.save(comprobante);
        return comprobanteMapper.toDto(comprobante);
    }

    @Override
    public Optional<ComprobanteDTO> partialUpdate(ComprobanteDTO comprobanteDTO) {
        LOG.debug("Request to partially update Comprobante : {}", comprobanteDTO);

        return comprobanteRepository
            .findById(comprobanteDTO.getId())
            .map(existingComprobante -> {
                comprobanteMapper.partialUpdate(existingComprobante, comprobanteDTO);

                return existingComprobante;
            })
            .map(comprobanteRepository::save)
            .map(comprobanteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComprobanteDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Comprobantes");
        return comprobanteRepository.findAll(pageable).map(comprobanteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ComprobanteDTO> findOne(Long id) {
        LOG.debug("Request to get Comprobante : {}", id);
        return comprobanteRepository.findById(id).map(comprobanteMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Comprobante : {}", id);
        comprobanteRepository.deleteById(id);
    }
}
