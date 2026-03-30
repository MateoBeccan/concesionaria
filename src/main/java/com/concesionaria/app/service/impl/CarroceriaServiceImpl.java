package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Carroceria;
import com.concesionaria.app.repository.CarroceriaRepository;
import com.concesionaria.app.service.CarroceriaService;
import com.concesionaria.app.service.dto.CarroceriaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.CarroceriaMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Carroceria}.
 */
@Service
@Transactional
public class CarroceriaServiceImpl implements CarroceriaService {

    private static final Logger LOG = LoggerFactory.getLogger(CarroceriaServiceImpl.class);

    private final CarroceriaRepository carroceriaRepository;

    private final CarroceriaMapper carroceriaMapper;

    public CarroceriaServiceImpl(CarroceriaRepository carroceriaRepository, CarroceriaMapper carroceriaMapper) {
        this.carroceriaRepository = carroceriaRepository;
        this.carroceriaMapper = carroceriaMapper;
    }

    @Override
    public CarroceriaDTO save(CarroceriaDTO carroceriaDTO) {
        LOG.debug("Request to save Carroceria : {}", carroceriaDTO);

        validarCarroceria(carroceriaDTO, null);

        Carroceria carroceria = carroceriaMapper.toEntity(carroceriaDTO);
        carroceria = carroceriaRepository.save(carroceria);

        return carroceriaMapper.toDto(carroceria);
    }

    @Override
    public CarroceriaDTO update(CarroceriaDTO carroceriaDTO) {
        LOG.debug("Request to update Carroceria : {}", carroceriaDTO);

        validarCarroceria(carroceriaDTO, carroceriaDTO.getId());

        Carroceria carroceria = carroceriaMapper.toEntity(carroceriaDTO);
        carroceria = carroceriaRepository.save(carroceria);

        return carroceriaMapper.toDto(carroceria);
    }

    @Override
    public Optional<CarroceriaDTO> partialUpdate(CarroceriaDTO carroceriaDTO) {
        LOG.debug("Request to partially update Carroceria : {}", carroceriaDTO);

        return carroceriaRepository
            .findById(carroceriaDTO.getId())
            .map(existingCarroceria -> {
                carroceriaMapper.partialUpdate(existingCarroceria, carroceriaDTO);

                validarCarroceria(carroceriaMapper.toDto(existingCarroceria), existingCarroceria.getId());

                return existingCarroceria;
            })
            .map(carroceriaRepository::save)
            .map(carroceriaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarroceriaDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Carrocerias");
        return carroceriaRepository.findAll(pageable).map(carroceriaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarroceriaDTO> findOne(Long id) {
        LOG.debug("Request to get Carroceria : {}", id);
        return carroceriaRepository.findById(id).map(carroceriaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Carroceria : {}", id);
        carroceriaRepository.deleteById(id);
    }

    private void validarCarroceria(CarroceriaDTO dto, Long idActual) {

        // nombre obligatorio
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la carrocería es obligatorio");
        }

        String nombre = dto.getNombre().trim().toUpperCase();

        // evitar duplicados
        Optional<Carroceria> existente = carroceriaRepository
            .findByNombreIgnoreCase(nombre);

        if (existente.isPresent() && !existente.get().getId().equals(idActual)) {
            throw new BadRequestException("Ya existe una carrocería con ese nombre");
        }

        // normalizar nombre
        dto.setNombre(nombre);
    }
}

