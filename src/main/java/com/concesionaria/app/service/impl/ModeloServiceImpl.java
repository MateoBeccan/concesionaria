package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.repository.ModeloRepository;
import com.concesionaria.app.service.ModeloService;
import com.concesionaria.app.service.dto.ModeloDTO;
import com.concesionaria.app.service.mapper.ModeloMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Modelo}.
 */
@Service
@Transactional
public class ModeloServiceImpl implements ModeloService {

    private static final Logger LOG = LoggerFactory.getLogger(ModeloServiceImpl.class);

    private final ModeloRepository modeloRepository;

    private final ModeloMapper modeloMapper;

    public ModeloServiceImpl(ModeloRepository modeloRepository, ModeloMapper modeloMapper) {
        this.modeloRepository = modeloRepository;
        this.modeloMapper = modeloMapper;
    }

    @Override
    public ModeloDTO save(ModeloDTO modeloDTO) {
        LOG.debug("Request to save Modelo : {}", modeloDTO);
        Modelo modelo = modeloMapper.toEntity(modeloDTO);
        modelo = modeloRepository.save(modelo);
        return modeloMapper.toDto(modelo);
    }

    @Override
    public ModeloDTO update(ModeloDTO modeloDTO) {
        LOG.debug("Request to update Modelo : {}", modeloDTO);
        Modelo modelo = modeloMapper.toEntity(modeloDTO);
        modelo = modeloRepository.save(modelo);
        return modeloMapper.toDto(modelo);
    }

    @Override
    public Optional<ModeloDTO> partialUpdate(ModeloDTO modeloDTO) {
        LOG.debug("Request to partially update Modelo : {}", modeloDTO);

        return modeloRepository
            .findById(modeloDTO.getId())
            .map(existingModelo -> {
                modeloMapper.partialUpdate(existingModelo, modeloDTO);

                return existingModelo;
            })
            .map(modeloRepository::save)
            .map(modeloMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModeloDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Modelos");
        return modeloRepository.findAll(pageable).map(modeloMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ModeloDTO> findOne(Long id) {
        LOG.debug("Request to get Modelo : {}", id);
        return modeloRepository.findById(id).map(modeloMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Modelo : {}", id);
        modeloRepository.deleteById(id);
    }
}
