package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Combustible;
import com.concesionaria.app.repository.CombustibleRepository;
import com.concesionaria.app.service.CombustibleService;
import com.concesionaria.app.service.dto.CombustibleDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.CombustibleMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Combustible}.
 */
@Service
@Transactional
public class CombustibleServiceImpl implements CombustibleService {

    private static final Logger LOG = LoggerFactory.getLogger(CombustibleServiceImpl.class);

    private final CombustibleRepository combustibleRepository;

    private final CombustibleMapper combustibleMapper;

    public CombustibleServiceImpl(CombustibleRepository combustibleRepository, CombustibleMapper combustibleMapper) {
        this.combustibleRepository = combustibleRepository;
        this.combustibleMapper = combustibleMapper;
    }

    @Override
    public CombustibleDTO save(CombustibleDTO combustibleDTO) {
        LOG.debug("Request to save Combustible : {}", combustibleDTO);

        validarCombustible(combustibleDTO, null);

        Combustible combustible = combustibleMapper.toEntity(combustibleDTO);
        combustible = combustibleRepository.save(combustible);

        return combustibleMapper.toDto(combustible);
    }

    @Override
    public CombustibleDTO update(CombustibleDTO combustibleDTO) {
        LOG.debug("Request to update Combustible : {}", combustibleDTO);

        validarCombustible(combustibleDTO, combustibleDTO.getId());

        Combustible combustible = combustibleMapper.toEntity(combustibleDTO);
        combustible = combustibleRepository.save(combustible);

        return combustibleMapper.toDto(combustible);
    }

    @Override
    public Optional<CombustibleDTO> partialUpdate(CombustibleDTO combustibleDTO) {
        LOG.debug("Request to partially update Combustible : {}", combustibleDTO);

        return combustibleRepository
            .findById(combustibleDTO.getId())
            .map(existing -> {
                combustibleMapper.partialUpdate(existing, combustibleDTO);

                validarCombustible(combustibleMapper.toDto(existing), existing.getId());

                return existing;
            })
            .map(combustibleRepository::save)
            .map(combustibleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CombustibleDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Combustibles");
        return combustibleRepository.findAll(pageable).map(combustibleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CombustibleDTO> findOne(Long id) {
        LOG.debug("Request to get Combustible : {}", id);
        return combustibleRepository.findById(id).map(combustibleMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Combustible : {}", id);
        combustibleRepository.deleteById(id);
    }

    private void validarCombustible(CombustibleDTO dto, Long idActual) {

        // nombre obligatorio
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del combustible es obligatorio");
        }

        String nombre = dto.getNombre().trim().toUpperCase();

        // evitar duplicados
        Optional<Combustible> existente = combustibleRepository
            .findByNombreIgnoreCase(nombre);

        if (existente.isPresent() && !existente.get().getId().equals(idActual)) {
            throw new BadRequestException("Ya existe un combustible con ese nombre");
        }

        // normalizar
        dto.setNombre(nombre);
    }
}
