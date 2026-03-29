package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.VentaDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VentaService {

    VentaDTO save(VentaDTO ventaDTO);

    VentaDTO update(VentaDTO ventaDTO);

    Optional<VentaDTO> partialUpdate(VentaDTO ventaDTO);

    Page<VentaDTO> findAll(Pageable pageable);

    Page<VentaDTO> findAllWithEagerRelationships(Pageable pageable);

    Optional<VentaDTO> findOne(Long id);

    void delete(Long id);

    // NEGOCIO
    VentaDTO crearVenta(Long vehiculoId, Long clienteId);
}
