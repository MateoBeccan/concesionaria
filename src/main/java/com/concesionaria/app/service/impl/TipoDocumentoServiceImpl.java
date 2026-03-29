package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.TipoDocumento;
import com.concesionaria.app.repository.TipoDocumentoRepository;
import com.concesionaria.app.service.TipoDocumentoService;
import com.concesionaria.app.service.dto.TipoDocumentoDTO;
import com.concesionaria.app.service.mapper.TipoDocumentoMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.TipoDocumento}.
 */
@Service
@Transactional
public class TipoDocumentoServiceImpl implements TipoDocumentoService {

    private static final Logger LOG = LoggerFactory.getLogger(TipoDocumentoServiceImpl.class);

    private final TipoDocumentoRepository tipoDocumentoRepository;

    private final TipoDocumentoMapper tipoDocumentoMapper;

    public TipoDocumentoServiceImpl(TipoDocumentoRepository tipoDocumentoRepository, TipoDocumentoMapper tipoDocumentoMapper) {
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.tipoDocumentoMapper = tipoDocumentoMapper;
    }

    @Override
    public TipoDocumentoDTO save(TipoDocumentoDTO tipoDocumentoDTO) {
        LOG.debug("Request to save TipoDocumento : {}", tipoDocumentoDTO);
        TipoDocumento tipoDocumento = tipoDocumentoMapper.toEntity(tipoDocumentoDTO);
        tipoDocumento = tipoDocumentoRepository.save(tipoDocumento);
        return tipoDocumentoMapper.toDto(tipoDocumento);
    }

    @Override
    public TipoDocumentoDTO update(TipoDocumentoDTO tipoDocumentoDTO) {
        LOG.debug("Request to update TipoDocumento : {}", tipoDocumentoDTO);
        TipoDocumento tipoDocumento = tipoDocumentoMapper.toEntity(tipoDocumentoDTO);
        tipoDocumento = tipoDocumentoRepository.save(tipoDocumento);
        return tipoDocumentoMapper.toDto(tipoDocumento);
    }

    @Override
    public Optional<TipoDocumentoDTO> partialUpdate(TipoDocumentoDTO tipoDocumentoDTO) {
        LOG.debug("Request to partially update TipoDocumento : {}", tipoDocumentoDTO);

        return tipoDocumentoRepository
            .findById(tipoDocumentoDTO.getId())
            .map(existingTipoDocumento -> {
                tipoDocumentoMapper.partialUpdate(existingTipoDocumento, tipoDocumentoDTO);

                return existingTipoDocumento;
            })
            .map(tipoDocumentoRepository::save)
            .map(tipoDocumentoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TipoDocumentoDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all TipoDocumentos");
        return tipoDocumentoRepository.findAll(pageable).map(tipoDocumentoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoDocumentoDTO> findOne(Long id) {
        LOG.debug("Request to get TipoDocumento : {}", id);
        return tipoDocumentoRepository.findById(id).map(tipoDocumentoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete TipoDocumento : {}", id);
        tipoDocumentoRepository.deleteById(id);
    }
}
