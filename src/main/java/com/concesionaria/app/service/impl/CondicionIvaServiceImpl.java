package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.CondicionIva;
import com.concesionaria.app.repository.CondicionIvaRepository;
import com.concesionaria.app.service.CondicionIvaService;
import com.concesionaria.app.service.dto.CondicionIvaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.CondicionIvaMapper;
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
public class CondicionIvaServiceImpl implements CondicionIvaService {

    private static final Logger LOG = LoggerFactory.getLogger(CondicionIvaServiceImpl.class);
    private final CondicionIvaRepository condicionIvaRepository;
    private final CondicionIvaMapper condicionIvaMapper;

    public CondicionIvaServiceImpl(CondicionIvaRepository condicionIvaRepository, CondicionIvaMapper condicionIvaMapper) {
        this.condicionIvaRepository = condicionIvaRepository;
        this.condicionIvaMapper = condicionIvaMapper;
    }

    @Override
    public CondicionIvaDTO save(CondicionIvaDTO condicionIvaDTO) {
        LOG.debug("Request to save CondicionIva : {}", condicionIvaDTO);
        normalizarYValidarCodigoUnico(condicionIvaDTO, null);
        CondicionIva condicionIva = condicionIvaMapper.toEntity(condicionIvaDTO);
        condicionIva = condicionIvaRepository.save(condicionIva);
        return condicionIvaMapper.toDto(condicionIva);
    }

    @Override
    public CondicionIvaDTO update(CondicionIvaDTO condicionIvaDTO) {
        LOG.debug("Request to update CondicionIva : {}", condicionIvaDTO);
        normalizarYValidarCodigoUnico(condicionIvaDTO, condicionIvaDTO.getId());
        CondicionIva condicionIva = condicionIvaMapper.toEntity(condicionIvaDTO);
        condicionIva = condicionIvaRepository.save(condicionIva);
        return condicionIvaMapper.toDto(condicionIva);
    }

    @Override
    public Optional<CondicionIvaDTO> partialUpdate(CondicionIvaDTO condicionIvaDTO) {
        LOG.debug("Request to partially update CondicionIva : {}", condicionIvaDTO);
        return condicionIvaRepository
            .findById(condicionIvaDTO.getId())
            .map(existingCondicionIva -> {
                condicionIvaMapper.partialUpdate(existingCondicionIva, condicionIvaDTO);
                CondicionIvaDTO dto = condicionIvaMapper.toDto(existingCondicionIva);
                normalizarYValidarCodigoUnico(dto, existingCondicionIva.getId());
                existingCondicionIva.setCodigo(dto.getCodigo());
                return existingCondicionIva;
            })
            .map(condicionIvaRepository::save)
            .map(condicionIvaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CondicionIvaDTO> findAll(Pageable pageable) {
        return condicionIvaRepository.findAll(pageable).map(condicionIvaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CondicionIvaDTO> findOne(Long id) {
        return condicionIvaRepository.findById(id).map(condicionIvaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        condicionIvaRepository.deleteById(id);
    }

    private void normalizarYValidarCodigoUnico(CondicionIvaDTO dto, Long idActual) {
        if (dto == null || dto.getCodigo() == null || dto.getCodigo().trim().isEmpty()) {
            throw new BadRequestException("El codigo de condicion IVA es obligatorio");
        }
        String codigo = dto.getCodigo().trim().toUpperCase(Locale.ROOT);
        dto.setCodigo(codigo);
        condicionIvaRepository
            .findByCodigoIgnoreCase(codigo)
            .filter(existing -> !existing.getId().equals(idActual))
            .ifPresent(existing -> {
                throw new BadRequestException("Ya existe una condicion IVA con ese codigo");
            });
    }
}
