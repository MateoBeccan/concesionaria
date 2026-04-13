package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.domain.VersionMotor;
import com.concesionaria.app.repository.ModeloRepository;
import com.concesionaria.app.repository.MotorRepository;
import com.concesionaria.app.repository.VersionMotorRepository;
import com.concesionaria.app.repository.VersionRepository;
import com.concesionaria.app.service.VersionService;
import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.dto.VersionDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.MotorMapper;
import com.concesionaria.app.service.mapper.VersionMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VersionServiceImpl implements VersionService {

    private static final Logger LOG = LoggerFactory.getLogger(VersionServiceImpl.class);

    private final VersionRepository versionRepository;
    private final VersionMotorRepository versionMotorRepository;
    private final MotorRepository motorRepository;
    private final VersionMapper versionMapper;
    private final MotorMapper motorMapper;
    private final ModeloRepository modeloRepository;

    public VersionServiceImpl(
        VersionRepository versionRepository,
        VersionMotorRepository versionMotorRepository,
        MotorRepository motorRepository,
        VersionMapper versionMapper,
        MotorMapper motorMapper,
        ModeloRepository modeloRepository
    ) {
        this.versionRepository = versionRepository;
        this.versionMotorRepository = versionMotorRepository;
        this.motorRepository = motorRepository;
        this.versionMapper = versionMapper;
        this.motorMapper = motorMapper;
        this.modeloRepository = modeloRepository;
    }

    @Override
    public VersionDTO save(VersionDTO versionDTO) {
        LOG.debug("Request to save Version : {}", versionDTO);
        validarVersion(versionDTO, null);
        Version version = versionMapper.toEntity(versionDTO);
        return versionMapper.toDto(versionRepository.save(version));
    }

    @Override
    public VersionDTO update(VersionDTO versionDTO) {
        LOG.debug("Request to update Version : {}", versionDTO);
        validarVersion(versionDTO, versionDTO.getId());

        Version existingVersion = validarExistenciaVersion(versionDTO.getId());
        Version version = versionMapper.toEntity(versionDTO);
        version.setId(existingVersion.getId());

        return versionMapper.toDto(versionRepository.save(version));
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
    @Transactional(readOnly = true)
    public List<MotorDTO> findMotorsByVersionId(Long id) {
        LOG.debug("Request to get motors for Version : {}", id);
        validarExistenciaVersion(id);
        return versionMotorRepository.findAllByVersionIdWithMotor(id).stream().map(VersionMotor::getMotor).map(motorMapper::toDto).toList();
    }

    @Override
    public List<MotorDTO> addMotorCompatibility(Long versionId, Long motorId) {
        LOG.debug("Request to add Motor {} to Version {}", motorId, versionId);
        Version version = validarExistenciaVersion(versionId);
        Motor motor = validarExistenciaMotor(motorId);

        if (versionMotorRepository.existsByVersionIdAndMotorId(versionId, motorId)) {
            throw new BadRequestException("Ese motor ya está asociado a la versión");
        }

        VersionMotor versionMotor = new VersionMotor();
        versionMotor.setVersion(version);
        versionMotor.setMotor(motor);
        versionMotorRepository.save(versionMotor);

        return findMotorsByVersionId(versionId);
    }

    @Override
    public List<MotorDTO> removeMotorCompatibility(Long versionId, Long motorId) {
        LOG.debug("Request to remove Motor {} from Version {}", motorId, versionId);
        validarExistenciaVersion(versionId);
        validarExistenciaMotor(motorId);

        VersionMotor versionMotor = versionMotorRepository
            .findByVersionIdAndMotorId(versionId, motorId)
            .orElseThrow(() -> new BadRequestException("Ese motor no está asociado a la versión"));

        versionMotorRepository.delete(versionMotor);
        return findMotorsByVersionId(versionId);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Version : {}", id);
        versionRepository.deleteById(id);
    }

    private void validarVersion(VersionDTO versionDTO, Long idActual) {
        if (versionDTO.getNombre() == null || versionDTO.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la versión es obligatorio");
        }

        if (versionDTO.getModelo() == null || versionDTO.getModelo().getId() == null) {
            throw new BadRequestException("La versión debe tener un modelo asociado");
        }

        if (!modeloRepository.existsById(versionDTO.getModelo().getId())) {
            throw new BadRequestException("El modelo no existe");
        }

        if (versionDTO.getAnioInicio() == null || versionDTO.getAnioInicio() < 1950) {
            throw new BadRequestException("Año de inicio inválido");
        }

        if (versionDTO.getAnioFin() != null && versionDTO.getAnioFin() < versionDTO.getAnioInicio()) {
            throw new BadRequestException("El año fin no puede ser menor al año inicio");
        }

        Optional<Version> existente = versionRepository.findByNombreIgnoreCaseAndModeloId(versionDTO.getNombre(), versionDTO.getModelo().getId());
        if (existente.isPresent() && !existente.get().getId().equals(idActual)) {
            throw new BadRequestException("Ya existe una versión con ese nombre para el modelo");
        }
    }

    private Version validarExistenciaVersion(Long versionId) {
        return versionRepository.findById(versionId).orElseThrow(() -> new BadRequestException("La versión no existe"));
    }

    private Motor validarExistenciaMotor(Long motorId) {
        return motorRepository.findById(motorId).orElseThrow(() -> new BadRequestException("El motor no existe"));
    }
}
