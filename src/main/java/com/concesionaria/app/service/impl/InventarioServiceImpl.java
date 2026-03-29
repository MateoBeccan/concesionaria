package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.service.InventarioService;
import com.concesionaria.app.service.dto.InventarioDTO;
import com.concesionaria.app.service.mapper.InventarioMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Inventario}.
 */
@Service
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private static final Logger LOG = LoggerFactory.getLogger(InventarioServiceImpl.class);

    private final InventarioRepository inventarioRepository;

    private final InventarioMapper inventarioMapper;

    public InventarioServiceImpl(InventarioRepository inventarioRepository, InventarioMapper inventarioMapper) {
        this.inventarioRepository = inventarioRepository;
        this.inventarioMapper = inventarioMapper;
    }

    @Override
    public InventarioDTO save(InventarioDTO inventarioDTO) {
        LOG.debug("Request to save Inventario : {}", inventarioDTO);
        Inventario inventario = inventarioMapper.toEntity(inventarioDTO);
        inventario = inventarioRepository.save(inventario);
        return inventarioMapper.toDto(inventario);
    }

    @Override
    public InventarioDTO update(InventarioDTO inventarioDTO) {
        LOG.debug("Request to update Inventario : {}", inventarioDTO);
        Inventario inventario = inventarioMapper.toEntity(inventarioDTO);
        inventario = inventarioRepository.save(inventario);
        return inventarioMapper.toDto(inventario);
    }

    @Override
    public Optional<InventarioDTO> partialUpdate(InventarioDTO inventarioDTO) {
        LOG.debug("Request to partially update Inventario : {}", inventarioDTO);

        return inventarioRepository
            .findById(inventarioDTO.getId())
            .map(existingInventario -> {
                inventarioMapper.partialUpdate(existingInventario, inventarioDTO);

                return existingInventario;
            })
            .map(inventarioRepository::save)
            .map(inventarioMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Inventarios");
        return inventarioRepository.findAll(pageable).map(inventarioMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventarioDTO> findOne(Long id) {
        LOG.debug("Request to get Inventario : {}", id);
        return inventarioRepository.findById(id).map(inventarioMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Inventario : {}", id);
        inventarioRepository.deleteById(id);
    }
}
