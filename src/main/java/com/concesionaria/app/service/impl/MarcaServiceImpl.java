package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.repository.MarcaRepository;
import com.concesionaria.app.service.MarcaService;
import com.concesionaria.app.service.dto.MarcaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.MarcaMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Marca}.
 */
@Service
@Transactional
public class MarcaServiceImpl implements MarcaService {

    private static final Logger LOG = LoggerFactory.getLogger(MarcaServiceImpl.class);

    private final MarcaRepository marcaRepository;

    private final MarcaMapper marcaMapper;

    public MarcaServiceImpl(MarcaRepository marcaRepository, MarcaMapper marcaMapper) {
        this.marcaRepository = marcaRepository;
        this.marcaMapper = marcaMapper;
    }

    @Override
    public MarcaDTO save(MarcaDTO marcaDTO) {
        LOG.debug("Request to save Marca : {}", marcaDTO);
        Marca marca = marcaMapper.toEntity(marcaDTO);
        marca = marcaRepository.save(marca);
        return marcaMapper.toDto(marca);
    }

    @Override
    public MarcaDTO update(MarcaDTO marcaDTO) {
        LOG.debug("Request to update Marca : {}", marcaDTO);
        Marca marca = marcaMapper.toEntity(marcaDTO);
        marca = marcaRepository.save(marca);
        return marcaMapper.toDto(marca);
    }

    @Override
    public Optional<MarcaDTO> partialUpdate(MarcaDTO marcaDTO) {
        LOG.debug("Request to partially update Marca : {}", marcaDTO);

        return marcaRepository
            .findById(marcaDTO.getId())
            .map(existingMarca -> {
                marcaMapper.partialUpdate(existingMarca, marcaDTO);

                return existingMarca;
            })
            .map(marcaRepository::save)
            .map(marcaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MarcaDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Marcas");
        return marcaRepository.findAll(pageable).map(marcaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MarcaDTO> findOne(Long id) {
        LOG.debug("Request to get Marca : {}", id);
        return marcaRepository.findById(id).map(marcaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Marca : {}", id);
        marcaRepository.deleteById(id);
    }

    private void validarMarca(MarcaDTO marcaDTO, Long idActual) {

        if (marcaDTO.getNombre() == null || marcaDTO.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la marca es obligatorio");
        }

        boolean existe = marcaRepository.existsByNombreIgnoreCase(marcaDTO.getNombre());

        if (existe) {
            if (idActual == null) {
                throw new BadRequestException("Ya existe una marca con ese nombre");
            }

            Marca existente = marcaRepository
                .findAll()
                .stream()
                .filter(m -> m.getNombre().equalsIgnoreCase(marcaDTO.getNombre()))
                .findFirst()
                .orElse(null);

            if (existente != null && !existente.getId().equals(idActual)) {
                throw new BadRequestException("Ya existe una marca con ese nombre");
            }
        }
    }
}
