package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.concesionaria.app.domain.Version}.
 */
public interface VersionService {
    VersionDTO save(VersionDTO versionDTO);

    VersionDTO update(VersionDTO versionDTO);

    Optional<VersionDTO> partialUpdate(VersionDTO versionDTO);

    Page<VersionDTO> findAll(Pageable pageable);

    Optional<VersionDTO> findOne(Long id);

    List<MotorDTO> findMotorsByVersionId(Long id);

    List<MotorDTO> addMotorCompatibility(Long versionId, Long motorId);

    List<MotorDTO> removeMotorCompatibility(Long versionId, Long motorId);

    void delete(Long id);
}
