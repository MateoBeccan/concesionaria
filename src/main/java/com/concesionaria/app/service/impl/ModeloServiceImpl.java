package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.repository.MarcaRepository;
import com.concesionaria.app.repository.ModeloRepository;
import com.concesionaria.app.service.ModeloService;
import com.concesionaria.app.service.dto.ModeloDTO;
import com.concesionaria.app.service.exception.BadRequestException;
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
    private final MarcaRepository marcaRepository;

    public ModeloServiceImpl(ModeloRepository modeloRepository, ModeloMapper modeloMapper, MarcaRepository marcaRepository) {
        this.modeloRepository = modeloRepository;
        this.modeloMapper = modeloMapper;
        this.marcaRepository = marcaRepository;
    }

    @Override
    public ModeloDTO save(ModeloDTO modeloDTO) {
        LOG.debug("Request to save Modelo : {}", modeloDTO);

        validarModelo(modeloDTO, null);

        Modelo modelo = modeloMapper.toEntity(modeloDTO);
        modelo = modeloRepository.save(modelo);

        return modeloMapper.toDto(modelo);
    }

    @Override
    public ModeloDTO update(ModeloDTO modeloDTO) {
        LOG.debug("Request to update Modelo : {}", modeloDTO);

        validarModelo(modeloDTO, modeloDTO.getId());

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

                validarModelo(modeloMapper.toDto(existingModelo), existingModelo.getId());

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

    // =========================
    // VALIDACIONES
    // =========================
    private void validarModelo(ModeloDTO modeloDTO, Long idActual) {

        // nombre obligatorio
        if (modeloDTO.getNombre() == null || modeloDTO.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del modelo es obligatorio");
        }

        // marca obligatoria
        if (modeloDTO.getMarca() == null || modeloDTO.getMarca().getId() == null) {
            throw new BadRequestException("El modelo debe tener una marca asociada");
        }

        // validar existencia de marca
        if (!marcaRepository.existsById(modeloDTO.getMarca().getId())) {
            throw new BadRequestException("La marca no existe");
        }

        // año válido
        if (modeloDTO.getAnioLanzamiento() == null ||
            modeloDTO.getAnioLanzamiento() < 1950 ||
            modeloDTO.getAnioLanzamiento() > 2026) {
            throw new BadRequestException("Año de lanzamiento inválido");
        }

        // validar duplicado (modelo + marca)
        Optional<Modelo> existente = modeloRepository
            .findByNombreIgnoreCaseAndMarcaId(
                modeloDTO.getNombre(),
                modeloDTO.getMarca().getId()
            );

        if (existente.isPresent() && !existente.get().getId().equals(idActual)) {
            throw new BadRequestException("Ya existe un modelo con ese nombre para la marca");
        }
    }
}
