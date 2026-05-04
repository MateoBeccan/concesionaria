package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.UbicacionStock;
import com.concesionaria.app.repository.UbicacionStockRepository;
import com.concesionaria.app.service.UbicacionStockService;
import com.concesionaria.app.service.dto.UbicacionStockDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.UbicacionStockMapper;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UbicacionStockServiceImpl implements UbicacionStockService {

    private final UbicacionStockRepository ubicacionStockRepository;
    private final UbicacionStockMapper ubicacionStockMapper;

    public UbicacionStockServiceImpl(UbicacionStockRepository ubicacionStockRepository, UbicacionStockMapper ubicacionStockMapper) {
        this.ubicacionStockRepository = ubicacionStockRepository;
        this.ubicacionStockMapper = ubicacionStockMapper;
    }

    @Override
    public UbicacionStockDTO save(UbicacionStockDTO dto) {
        validarUnicidadCodigo(dto, null);
        UbicacionStock entity = ubicacionStockMapper.toEntity(dto);
        return ubicacionStockMapper.toDto(ubicacionStockRepository.save(entity));
    }

    @Override
    public UbicacionStockDTO update(UbicacionStockDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestException("La ubicacion debe tener id para actualizar");
        }
        validarUnicidadCodigo(dto, dto.getId());
        UbicacionStock entity = ubicacionStockMapper.toEntity(dto);
        return ubicacionStockMapper.toDto(ubicacionStockRepository.save(entity));
    }

    @Override
    public Optional<UbicacionStockDTO> partialUpdate(UbicacionStockDTO dto) {
        return ubicacionStockRepository
            .findById(dto.getId())
            .map(existing -> {
                ubicacionStockMapper.partialUpdate(existing, dto);
                validarUnicidadCodigo(ubicacionStockMapper.toDto(existing), existing.getId());
                return existing;
            })
            .map(ubicacionStockRepository::save)
            .map(ubicacionStockMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UbicacionStockDTO> findAll(Pageable pageable) {
        return ubicacionStockRepository.findAll(pageable).map(ubicacionStockMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UbicacionStockDTO> findOne(Long id) {
        return ubicacionStockRepository.findById(id).map(ubicacionStockMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        ubicacionStockRepository.deleteById(id);
    }

    private void validarUnicidadCodigo(UbicacionStockDTO dto, Long idActual) {
        if (dto == null || dto.getCodigo() == null || dto.getCodigo().isBlank()) {
            throw new BadRequestException("El codigo de ubicacion es obligatorio");
        }
        ubicacionStockRepository
            .findOneByCodigoIgnoreCase(dto.getCodigo())
            .filter(existing -> !Objects.equals(existing.getId(), idActual))
            .ifPresent(existing -> {
                throw new BadRequestException("Ya existe una ubicacion con ese codigo");
            });
    }
}
