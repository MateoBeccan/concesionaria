package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.TipoCaja;
import com.concesionaria.app.repository.TipoCajaRepository;
import com.concesionaria.app.service.TipoCajaService;
import com.concesionaria.app.service.dto.TipoCajaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.TipoCajaMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.TipoCaja}.
 */
@Service
@Transactional
public class TipoCajaServiceImpl implements TipoCajaService {

    private static final Logger LOG = LoggerFactory.getLogger(TipoCajaServiceImpl.class);

    private final TipoCajaRepository tipoCajaRepository;

    private final TipoCajaMapper tipoCajaMapper;

    public TipoCajaServiceImpl(TipoCajaRepository tipoCajaRepository, TipoCajaMapper tipoCajaMapper) {
        this.tipoCajaRepository = tipoCajaRepository;
        this.tipoCajaMapper = tipoCajaMapper;
    }

    @Override
    public TipoCajaDTO save(TipoCajaDTO tipoCajaDTO) {
        LOG.debug("Request to save TipoCaja : {}", tipoCajaDTO);

        validarTipoCaja(tipoCajaDTO, null);

        TipoCaja tipoCaja = tipoCajaMapper.toEntity(tipoCajaDTO);
        tipoCaja = tipoCajaRepository.save(tipoCaja);

        return tipoCajaMapper.toDto(tipoCaja);
    }

    @Override
    public TipoCajaDTO update(TipoCajaDTO tipoCajaDTO) {
        LOG.debug("Request to update TipoCaja : {}", tipoCajaDTO);

        validarTipoCaja(tipoCajaDTO, tipoCajaDTO.getId());

        TipoCaja tipoCaja = tipoCajaMapper.toEntity(tipoCajaDTO);
        tipoCaja = tipoCajaRepository.save(tipoCaja);

        return tipoCajaMapper.toDto(tipoCaja);
    }

    @Override
    public Optional<TipoCajaDTO> partialUpdate(TipoCajaDTO tipoCajaDTO) {
        LOG.debug("Request to partially update TipoCaja : {}", tipoCajaDTO);

        return tipoCajaRepository
            .findById(tipoCajaDTO.getId())
            .map(existingTipoCaja -> {
                tipoCajaMapper.partialUpdate(existingTipoCaja, tipoCajaDTO);

                validarTipoCaja(tipoCajaMapper.toDto(existingTipoCaja), existingTipoCaja.getId());

                return existingTipoCaja;
            })
            .map(tipoCajaRepository::save)
            .map(tipoCajaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TipoCajaDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all TipoCajas");
        return tipoCajaRepository.findAll(pageable).map(tipoCajaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoCajaDTO> findOne(Long id) {
        LOG.debug("Request to get TipoCaja : {}", id);
        return tipoCajaRepository.findById(id).map(tipoCajaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete TipoCaja : {}", id);
        tipoCajaRepository.deleteById(id);
    }

    private void validarTipoCaja(TipoCajaDTO dto, Long idActual) {

        // nombre obligatorio
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del tipo de caja es obligatorio");
        }

        String nombre = dto.getNombre().trim().toUpperCase();

        // evitar duplicados
        Optional<TipoCaja> existente = tipoCajaRepository
            .findByNombreIgnoreCase(nombre);

        if (existente.isPresent() && !existente.get().getId().equals(idActual)) {
            throw new BadRequestException("Ya existe un tipo de caja con ese nombre");
        }

        // normalizar
        dto.setNombre(nombre);
    }
}
