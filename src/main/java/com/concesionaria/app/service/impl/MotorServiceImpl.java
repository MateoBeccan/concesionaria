package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.repository.CombustibleRepository;
import com.concesionaria.app.repository.MotorRepository;
import com.concesionaria.app.repository.TipoCajaRepository;
import com.concesionaria.app.repository.TraccionRepository;
import com.concesionaria.app.service.MotorService;
import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.MotorMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CombustibleRepository combustibleRepository;
    private final TipoCajaRepository tipoCajaRepository;
    private final TraccionRepository traccionRepository;

    public MotorServiceImpl(MotorRepository motorRepository, MotorMapper motorMapper, CombustibleRepository combustibleRepository, TipoCajaRepository tipoCajaRepository, TraccionRepository traccionRepository) {
        this.motorRepository = motorRepository;
        this.motorMapper = motorMapper;
        this.combustibleRepository = combustibleRepository;
        this.tipoCajaRepository = tipoCajaRepository;
        this.traccionRepository = traccionRepository;
    }

    @Override
    public MotorDTO save(MotorDTO motorDTO) {
        LOG.debug("Request to save Motor : {}", motorDTO);

        validarMotor(motorDTO, null);

        Motor motor = motorMapper.toEntity(motorDTO);
        motor = motorRepository.save(motor);

        return motorMapper.toDto(motor);
    }

    @Override
    public MotorDTO update(MotorDTO motorDTO) {
        LOG.debug("Request to update Motor : {}", motorDTO);

        validarMotor(motorDTO, motorDTO.getId());

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

                validarMotor(motorMapper.toDto(existingMotor), existingMotor.getId());

                return existingMotor;
            })
            .map(motorRepository::save)
            .map(motorMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MotorDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Motors");
        return motorRepository.findAll(pageable).map(motorMapper::toDto);
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

    // =========================
    // VALIDACIONES
    // =========================

    private void validarMotor(MotorDTO motorDTO, Long idActual) {


        if (!combustibleRepository.existsById(motorDTO.getCombustible().getId())) {
            throw new BadRequestException("El combustible no existe");
        }

        if (!tipoCajaRepository.existsById(motorDTO.getTipoCaja().getId())) {
            throw new BadRequestException("El tipo de caja no existe");
        }

        if (!traccionRepository.existsById(motorDTO.getTraccion().getId())) {
            throw new BadRequestException("La tracción no existe");
        }

        // nombre obligatorio
        if (motorDTO.getNombre() == null || motorDTO.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del motor es obligatorio");
        }

        // cilindrada válida
        if (motorDTO.getCilindradaCc() == null ||
            motorDTO.getCilindradaCc() < 50 ||
            motorDTO.getCilindradaCc() > 10000) {
            throw new RuntimeException("Cilindrada inválida");
        }

        // potencia válida
        if (motorDTO.getPotenciaHp() == null ||
            motorDTO.getPotenciaHp() <= 0 ||
            motorDTO.getPotenciaHp() > 2000) {
            throw new RuntimeException("Potencia inválida");
        }

        // cilindros válidos
        if (motorDTO.getCilindroCant() == null ||
            motorDTO.getCilindroCant() <= 0 ||
            motorDTO.getCilindroCant() > 16) {
            throw new RuntimeException("Cantidad de cilindros inválida");
        }

        // relaciones obligatorias
        if (motorDTO.getCombustible() == null || motorDTO.getCombustible().getId() == null) {
            throw new RuntimeException("Debe especificar combustible");
        }

        if (motorDTO.getTipoCaja() == null || motorDTO.getTipoCaja().getId() == null) {
            throw new RuntimeException("Debe especificar tipo de caja");
        }

        if (motorDTO.getTraccion() == null || motorDTO.getTraccion().getId() == null) {
            throw new RuntimeException("Debe especificar tracción");
        }

        // evitar duplicados técnicos
        boolean existe = motorRepository.existsByNombreIgnoreCase(motorDTO.getNombre());

        if (existe) {
            if (idActual == null) {
                throw new RuntimeException("Ya existe un motor con ese nombre");
            }
        }
    }
}
