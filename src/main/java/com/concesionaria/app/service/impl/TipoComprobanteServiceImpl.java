package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.repository.TipoComprobanteRepository;
import com.concesionaria.app.service.TipoComprobanteService;
import com.concesionaria.app.service.dto.TipoComprobanteDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.TipoComprobanteMapper;
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
public class TipoComprobanteServiceImpl implements TipoComprobanteService {

    private static final Logger LOG = LoggerFactory.getLogger(TipoComprobanteServiceImpl.class);
    private final TipoComprobanteRepository tipoComprobanteRepository;
    private final TipoComprobanteMapper tipoComprobanteMapper;

    public TipoComprobanteServiceImpl(TipoComprobanteRepository tipoComprobanteRepository, TipoComprobanteMapper tipoComprobanteMapper) {
        this.tipoComprobanteRepository = tipoComprobanteRepository;
        this.tipoComprobanteMapper = tipoComprobanteMapper;
    }

    @Override
    public TipoComprobanteDTO save(TipoComprobanteDTO tipoComprobanteDTO) {
        LOG.debug("Request to save TipoComprobante : {}", tipoComprobanteDTO);
        normalizarYValidarCodigoUnico(tipoComprobanteDTO, null);
        TipoComprobante tipoComprobante = tipoComprobanteMapper.toEntity(tipoComprobanteDTO);
        tipoComprobante = tipoComprobanteRepository.save(tipoComprobante);
        return tipoComprobanteMapper.toDto(tipoComprobante);
    }

    @Override
    public TipoComprobanteDTO update(TipoComprobanteDTO tipoComprobanteDTO) {
        LOG.debug("Request to update TipoComprobante : {}", tipoComprobanteDTO);
        normalizarYValidarCodigoUnico(tipoComprobanteDTO, tipoComprobanteDTO.getId());
        TipoComprobante tipoComprobante = tipoComprobanteMapper.toEntity(tipoComprobanteDTO);
        tipoComprobante = tipoComprobanteRepository.save(tipoComprobante);
        return tipoComprobanteMapper.toDto(tipoComprobante);
    }

    @Override
    public Optional<TipoComprobanteDTO> partialUpdate(TipoComprobanteDTO tipoComprobanteDTO) {
        LOG.debug("Request to partially update TipoComprobante : {}", tipoComprobanteDTO);
        return tipoComprobanteRepository
            .findById(tipoComprobanteDTO.getId())
            .map(existingTipoComprobante -> {
                tipoComprobanteMapper.partialUpdate(existingTipoComprobante, tipoComprobanteDTO);
                TipoComprobanteDTO dto = tipoComprobanteMapper.toDto(existingTipoComprobante);
                normalizarYValidarCodigoUnico(dto, existingTipoComprobante.getId());
                existingTipoComprobante.setCodigo(dto.getCodigo());
                return existingTipoComprobante;
            })
            .map(tipoComprobanteRepository::save)
            .map(tipoComprobanteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TipoComprobanteDTO> findAll(Pageable pageable) {
        return tipoComprobanteRepository.findAll(pageable).map(tipoComprobanteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoComprobanteDTO> findOne(Long id) {
        return tipoComprobanteRepository.findById(id).map(tipoComprobanteMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        tipoComprobanteRepository.deleteById(id);
    }

    private void normalizarYValidarCodigoUnico(TipoComprobanteDTO dto, Long idActual) {
        if (dto == null || dto.getCodigo() == null || dto.getCodigo().trim().isEmpty()) {
            throw new BadRequestException("El codigo de tipo comprobante es obligatorio");
        }
        String codigo = dto.getCodigo().trim().toUpperCase(Locale.ROOT);
        dto.setCodigo(codigo);
        tipoComprobanteRepository
            .findByCodigoIgnoreCase(codigo)
            .filter(existing -> !existing.getId().equals(idActual))
            .ifPresent(existing -> {
                throw new BadRequestException("Ya existe un tipo de comprobante con ese codigo");
            });
    }
}
