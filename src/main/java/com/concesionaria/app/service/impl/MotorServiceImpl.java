package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.repository.MotorRepository;
import com.concesionaria.app.service.MotorService;
import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.mapper.MotorMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Motor}.
 */
@Service
@Transactional
public class MotorServiceImpl implements MotorService {

    private static final Logger LOG = LoggerFactory.getLogger(MotorServiceImpl.class);

    private final MotorRepository motorRepository;

    private final MotorMapper motorMapper;

    public MotorServiceImpl(MotorRepository motorRepository, MotorMapper motorMapper) {
        this.motorRepository = motorRepository;
        this.motorMapper = motorMapper;
    }

    @Override
    public MotorDTO save(MotorDTO motorDTO) {
        LOG.debug("Request to save Motor : {}", motorDTO);
        Motor motor = motorMapper.toEntity(motorDTO);
        motor = motorRepository.save(motor);
        return motorMapper.toDto(motor);
    }

    @Override
    public MotorDTO update(MotorDTO motorDTO) {
        LOG.debug("Request to update Motor : {}", motorDTO);
        Motor motor = motorMapper.toEntity(motorDTO);
        motor = motorRepository.save(motor);
        return motorMapper.toDto(motor);
    }

    @Override
    public Optional<MotorDTO> partialUpdate(MotorDTO motorDTO) {
        LOG.debug("Request to partially update Motor : {}", motorDTO);

        return motorRepository
            .findById(motorDTO.getId())
            .map(existingMotor -> {
                motorMapper.partialUpdate(existingMotor, motorDTO);

                return existingMotor;
            })
            .map(motorRepository::save)
            .map(motorMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MotorDTO> findOne(Long id) {
        LOG.debug("Request to get Motor : {}", id);
        return motorRepository.findById(id).map(motorMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Motor : {}", id);
        motorRepository.deleteById(id);
    }
}
