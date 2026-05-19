package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.dto.VentaHistorialDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VentaService {

    VentaDTO save(VentaDTO ventaDTO);
    VentaDTO saveDesdePlanAhorro(VentaDTO ventaDTO);

    VentaDTO update(VentaDTO ventaDTO);

    Optional<VentaDTO> partialUpdate(VentaDTO ventaDTO);

    Page<VentaDTO> findAll(Pageable pageable);

    Page<VentaDTO> findAllWithEagerRelationships(Pageable pageable);

    Page<VentaDTO> findAllCurrentUser(Pageable pageable);

    Page<VentaDTO> findAllCurrentUserWithEagerRelationships(Pageable pageable);

    Optional<VentaDTO> findOne(Long id);

    Optional<VentaDTO> findByReservaId(Long reservaId);

    void delete(Long id);


    VentaDTO crearVenta(Long vehiculoId, Long clienteId);

    void confirmarVenta(Long ventaId);

    void sincronizarInventarioConVenta(Long ventaId);

    List<VentaHistorialDTO> findHistorialByVentaId(Long ventaId);

}
