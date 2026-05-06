package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.TasacionUsadoDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TasacionUsadoService {
    TasacionUsadoDTO save(TasacionUsadoDTO dto);
    TasacionUsadoDTO update(TasacionUsadoDTO dto);
    Optional<TasacionUsadoDTO> partialUpdate(TasacionUsadoDTO dto);
    Page<TasacionUsadoDTO> findAll(Pageable pageable);
    Page<TasacionUsadoDTO> findAllCurrentUser(Pageable pageable);
    Optional<TasacionUsadoDTO> findOne(Long id);
    List<TasacionUsadoDTO> findByVentaId(Long ventaId);
    List<TasacionUsadoDTO> findAceptadasDisponiblesByClienteId(Long clienteId);
    void delete(Long id);
}
