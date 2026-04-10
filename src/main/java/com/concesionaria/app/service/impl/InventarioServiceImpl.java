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

    private final InventarioRepository inventarioRepository;
    private final InventarioMapper inventarioMapper;

    public InventarioServiceImpl(InventarioRepository inventarioRepository, InventarioMapper inventarioMapper) {
        this.inventarioRepository = inventarioRepository;
        this.inventarioMapper = inventarioMapper;
    }

    @Override
    public InventarioDTO save(InventarioDTO dto) {
        return inventarioMapper.toDto(inventarioRepository.save(inventarioMapper.toEntity(dto)));
    }

    @Override
    public InventarioDTO update(InventarioDTO dto) {
        return inventarioMapper.toDto(inventarioRepository.save(inventarioMapper.toEntity(dto)));
    }

    @Override
    public Optional<InventarioDTO> partialUpdate(InventarioDTO dto) {
        return inventarioRepository.findById(dto.getId())
            .map(existing -> {
                inventarioMapper.partialUpdate(existing, dto);
                return existing;
            })
            .map(inventarioRepository::save)
            .map(inventarioMapper::toDto);
    }

    @Override
    public Page<InventarioDTO> findAll(Pageable pageable) {
        return inventarioRepository.findAll(pageable).map(inventarioMapper::toDto);
    }

    @Override
    public Optional<InventarioDTO> findOne(Long id) {
        return inventarioRepository.findById(id).map(inventarioMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        inventarioRepository.deleteById(id);
    }
}
