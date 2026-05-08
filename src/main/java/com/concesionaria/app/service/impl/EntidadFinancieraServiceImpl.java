package com.concesionaria.app.service.impl;

import com.concesionaria.app.repository.EntidadFinancieraRepository;
import com.concesionaria.app.service.EntidadFinancieraService;
import com.concesionaria.app.service.dto.EntidadFinancieraDTO;
import com.concesionaria.app.service.mapper.EntidadFinancieraMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EntidadFinancieraServiceImpl implements EntidadFinancieraService {

    private final EntidadFinancieraRepository entidadFinancieraRepository;
    private final EntidadFinancieraMapper entidadFinancieraMapper;

    public EntidadFinancieraServiceImpl(
        EntidadFinancieraRepository entidadFinancieraRepository,
        EntidadFinancieraMapper entidadFinancieraMapper
    ) {
        this.entidadFinancieraRepository = entidadFinancieraRepository;
        this.entidadFinancieraMapper = entidadFinancieraMapper;
    }

    @Override
    public List<EntidadFinancieraDTO> findAllActivas() {
        return entidadFinancieraRepository.findByActivaTrueOrderByNombreAsc().stream().map(entidadFinancieraMapper::toDto).toList();
    }
}
