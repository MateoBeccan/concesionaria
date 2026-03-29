package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.TipoVehiculo;
import com.concesionaria.app.repository.TipoVehiculoRepository;
import com.concesionaria.app.service.TipoVehiculoService;
import com.concesionaria.app.service.dto.TipoVehiculoDTO;
import com.concesionaria.app.service.mapper.TipoVehiculoMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.TipoVehiculo}.
 */
@Service
@Transactional
public class TipoVehiculoServiceImpl implements TipoVehiculoService {

    private static final Logger LOG = LoggerFactory.getLogger(TipoVehiculoServiceImpl.class);

    private final TipoVehiculoRepository tipoVehiculoRepository;

    private final TipoVehiculoMapper tipoVehiculoMapper;

    public TipoVehiculoServiceImpl(TipoVehiculoRepository tipoVehiculoRepository, TipoVehiculoMapper tipoVehiculoMapper) {
        this.tipoVehiculoRepository = tipoVehiculoRepository;
        this.tipoVehiculoMapper = tipoVehiculoMapper;
    }

    @Override
    public TipoVehiculoDTO save(TipoVehiculoDTO tipoVehiculoDTO) {
        LOG.debug("Request to save TipoVehiculo : {}", tipoVehiculoDTO);
        TipoVehiculo tipoVehiculo = tipoVehiculoMapper.toEntity(tipoVehiculoDTO);
        tipoVehiculo = tipoVehiculoRepository.save(tipoVehiculo);
        return tipoVehiculoMapper.toDto(tipoVehiculo);
    }

    @Override
    public TipoVehiculoDTO update(TipoVehiculoDTO tipoVehiculoDTO) {
        LOG.debug("Request to update TipoVehiculo : {}", tipoVehiculoDTO);
        TipoVehiculo tipoVehiculo = tipoVehiculoMapper.toEntity(tipoVehiculoDTO);
        tipoVehiculo = tipoVehiculoRepository.save(tipoVehiculo);
        return tipoVehiculoMapper.toDto(tipoVehiculo);
    }

    @Override
    public Optional<TipoVehiculoDTO> partialUpdate(TipoVehiculoDTO tipoVehiculoDTO) {
        LOG.debug("Request to partially update TipoVehiculo : {}", tipoVehiculoDTO);

        return tipoVehiculoRepository
            .findById(tipoVehiculoDTO.getId())
            .map(existingTipoVehiculo -> {
                tipoVehiculoMapper.partialUpdate(existingTipoVehiculo, tipoVehiculoDTO);

                return existingTipoVehiculo;
            })
            .map(tipoVehiculoRepository::save)
            .map(tipoVehiculoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TipoVehiculoDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all TipoVehiculos");
        return tipoVehiculoRepository.findAll(pageable).map(tipoVehiculoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoVehiculoDTO> findOne(Long id) {
        LOG.debug("Request to get TipoVehiculo : {}", id);
        return tipoVehiculoRepository.findById(id).map(tipoVehiculoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete TipoVehiculo : {}", id);
        tipoVehiculoRepository.deleteById(id);
    }
}
