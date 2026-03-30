package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Version;
import com.concesionaria.app.repository.VersionRepository;
import com.concesionaria.app.service.VersionService;
import com.concesionaria.app.service.dto.VersionDTO;
import com.concesionaria.app.service.mapper.VersionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Version}.
 */
@Service
@Transactional
public class VersionServiceImpl implements VersionService {

    private static final Logger LOG = LoggerFactory.getLogger(VersionServiceImpl.class);

    private final VersionRepository versionRepository;
    private final VersionMapper versionMapper;

    public VersionServiceImpl(VersionRepository versionRepository, VersionMapper versionMapper) {
        this.versionRepository = versionRepository;
        this.versionMapper = versionMapper;
    }

    @Override
    public VersionDTO save(VersionDTO versionDTO) {
        LOG.debug("Request to save Version : {}", versionDTO);

        validarVersion(versionDTO, null);

        Version version = versionMapper.toEntity(versionDTO);
        version = versionRepository.save(version);

        return versionMapper.toDto(version);
    }

    @Override
    public VersionDTO update(VersionDTO versionDTO) {
        LOG.debug("Request to update Version : {}", versionDTO);

        validarVersion(versionDTO, versionDTO.getId());

        Version version = versionMapper.toEntity(versionDTO);
        version = versionRepository.save(version);

        return versionMapper.toDto(version);
    }

    @Override
    public Optional<VersionDTO> partialUpdate(VersionDTO versionDTO) {
        LOG.debug("Request to partially update Version : {}", versionDTO);

        return versionRepository
            .findById(versionDTO.getId())
            .map(existingVersion -> {
                versionMapper.partialUpdate(existingVersion, versionDTO);

                validarVersion(versionMapper.toDto(existingVersion), existingVersion.getId());

                return existingVersion;
            })
            .map(versionRepository::save)
            .map(versionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VersionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Versions");
        return versionRepository.findAll(pageable).map(versionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VersionDTO> findOne(Long id) {
        LOG.debug("Request to get Version : {}", id);
        return versionRepository.findById(id).map(versionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Version : {}", id);
        versionRepository.deleteById(id);
    }

    // ========================
    // VALIDACIONES
    // ========================

    private void validarVersion(VersionDTO versionDTO, Long idActual) {

        // nombre obligatorio
        if (versionDTO.getNombre() == null || versionDTO.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre de la versión es obligatorio");
        }

        // modelo obligatorio
        if (versionDTO.getModelo() == null || versionDTO.getModelo().getId() == null) {
            throw new RuntimeException("La versión debe tener un modelo asociado");
        }

        // año inicio obligatorio
        if (versionDTO.getAnioInicio() == null || versionDTO.getAnioInicio() < 1950) {
            throw new RuntimeException("Año de inicio inválido");
        }

        // año fin coherente
        if (versionDTO.getAnioFin() != null) {
            if (versionDTO.getAnioFin() < versionDTO.getAnioInicio()) {
                throw new RuntimeException("El año fin no puede ser menor al año inicio");
            }
        }

        // evitar duplicados (version + modelo)
        boolean existe = versionRepository.existsByNombreIgnoreCaseAndModeloId(
            versionDTO.getNombre(),
            versionDTO.getModelo().getId()
        );

        if (existe) {
            if (idActual == null) {
                throw new RuntimeException("Ya existe una versión con ese nombre para el modelo");
            }

            Version existente = versionRepository
                .findAll()
                .stream()
                .filter(v ->
                    v.getNombre().equalsIgnoreCase(versionDTO.getNombre()) &&
                        v.getModelo().getId().equals(versionDTO.getModelo().getId())
                )
                .findFirst()
                .orElse(null);

            if (existente != null && !existente.getId().equals(idActual)) {
                throw new RuntimeException("Ya existe una versión con ese nombre para el modelo");
            }
        }
    }
}
