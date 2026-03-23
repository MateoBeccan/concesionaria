package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.service.MetodoPagoService;
import com.concesionaria.app.service.dto.MetodoPagoDTO;
import com.concesionaria.app.service.mapper.MetodoPagoMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.MetodoPago}.
 */
@Service
@Transactional
public class MetodoPagoServiceImpl implements MetodoPagoService {

    private static final Logger LOG = LoggerFactory.getLogger(MetodoPagoServiceImpl.class);

    private final MetodoPagoRepository metodoPagoRepository;

    private final MetodoPagoMapper metodoPagoMapper;

    public MetodoPagoServiceImpl(MetodoPagoRepository metodoPagoRepository, MetodoPagoMapper metodoPagoMapper) {
        this.metodoPagoRepository = metodoPagoRepository;
        this.metodoPagoMapper = metodoPagoMapper;
    }

    @Override
    public MetodoPagoDTO save(MetodoPagoDTO metodoPagoDTO) {
        LOG.debug("Request to save MetodoPago : {}", metodoPagoDTO);
        MetodoPago metodoPago = metodoPagoMapper.toEntity(metodoPagoDTO);
        metodoPago = metodoPagoRepository.save(metodoPago);
        return metodoPagoMapper.toDto(metodoPago);
    }

    @Override
    public MetodoPagoDTO update(MetodoPagoDTO metodoPagoDTO) {
        LOG.debug("Request to update MetodoPago : {}", metodoPagoDTO);
        MetodoPago metodoPago = metodoPagoMapper.toEntity(metodoPagoDTO);
        metodoPago = metodoPagoRepository.save(metodoPago);
        return metodoPagoMapper.toDto(metodoPago);
    }

    @Override
    public Optional<MetodoPagoDTO> partialUpdate(MetodoPagoDTO metodoPagoDTO) {
        LOG.debug("Request to partially update MetodoPago : {}", metodoPagoDTO);

        return metodoPagoRepository
            .findById(metodoPagoDTO.getId())
            .map(existingMetodoPago -> {
                metodoPagoMapper.partialUpdate(existingMetodoPago, metodoPagoDTO);

                return existingMetodoPago;
            })
            .map(metodoPagoRepository::save)
            .map(metodoPagoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MetodoPagoDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all MetodoPagos");
        return metodoPagoRepository.findAll(pageable).map(metodoPagoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MetodoPagoDTO> findOne(Long id) {
        LOG.debug("Request to get MetodoPago : {}", id);
        return metodoPagoRepository.findById(id).map(metodoPagoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete MetodoPago : {}", id);
        metodoPagoRepository.deleteById(id);
    }
}
