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
        Version version = versionMapper.toEntity(versionDTO);
        version = versionRepository.save(version);
        return versionMapper.toDto(version);
    }

    @Override
    public VersionDTO update(VersionDTO versionDTO) {
        LOG.debug("Request to update Version : {}", versionDTO);
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

                return existingVersion;
            })
            .map(versionRepository::save)
            .map(versionMapper::toDto);
    }

    public Page<VersionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return versionRepository.findAllWithEagerRelationships(pageable).map(versionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VersionDTO> findOne(Long id) {
        LOG.debug("Request to get Version : {}", id);
        return versionRepository.findOneWithEagerRelationships(id).map(versionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Version : {}", id);
        versionRepository.deleteById(id);
    }
}
