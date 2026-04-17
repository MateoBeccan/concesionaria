package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.service.MonedaService;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.MonedaMapper;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MonedaServiceImpl implements MonedaService {

    private static final Logger LOG = LoggerFactory.getLogger(MonedaServiceImpl.class);
    private final MonedaRepository monedaRepository;
    private final MonedaMapper monedaMapper;

    public MonedaServiceImpl(MonedaRepository monedaRepository, MonedaMapper monedaMapper) {
        this.monedaRepository = monedaRepository;
        this.monedaMapper = monedaMapper;
    }

    @Override
    public MonedaDTO save(MonedaDTO monedaDTO) {
        LOG.debug("Request to save Moneda : {}", monedaDTO);
        normalizarYValidarCodigoUnico(monedaDTO, null);
        Moneda moneda = monedaMapper.toEntity(monedaDTO);
        moneda = monedaRepository.save(moneda);
        return monedaMapper.toDto(moneda);
    }

    @Override
    public MonedaDTO update(MonedaDTO monedaDTO) {
        LOG.debug("Request to update Moneda : {}", monedaDTO);
        normalizarYValidarCodigoUnico(monedaDTO, monedaDTO.getId());
        Moneda moneda = monedaMapper.toEntity(monedaDTO);
        moneda = monedaRepository.save(moneda);
        return monedaMapper.toDto(moneda);
    }

    @Override
    public Optional<MonedaDTO> partialUpdate(MonedaDTO monedaDTO) {
        LOG.debug("Request to partially update Moneda : {}", monedaDTO);
        return monedaRepository
            .findById(monedaDTO.getId())
            .map(existingMoneda -> {
                monedaMapper.partialUpdate(existingMoneda, monedaDTO);
                MonedaDTO dto = monedaMapper.toDto(existingMoneda);
                normalizarYValidarCodigoUnico(dto, existingMoneda.getId());
                existingMoneda.setCodigo(dto.getCodigo());
                return existingMoneda;
            })
            .map(monedaRepository::save)
            .map(monedaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MonedaDTO> findAll(Pageable pageable) {
        return monedaRepository.findAll(pageable).map(monedaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MonedaDTO> findOne(Long id) {
        return monedaRepository.findById(id).map(monedaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        monedaRepository.deleteById(id);
    }

    private void normalizarYValidarCodigoUnico(MonedaDTO dto, Long idActual) {
        if (dto == null || dto.getCodigo() == null || dto.getCodigo().trim().isEmpty()) {
            throw new BadRequestException("El codigo de moneda es obligatorio");
        }
        String codigo = dto.getCodigo().trim().toUpperCase(Locale.ROOT);
        dto.setCodigo(codigo);
        monedaRepository
            .findByCodigoIgnoreCase(codigo)
            .filter(existing -> !existing.getId().equals(idActual))
            .ifPresent(existing -> {
                throw new BadRequestException("Ya existe una moneda con ese codigo");
            });
    }
}
