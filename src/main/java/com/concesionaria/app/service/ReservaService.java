package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.ReservaDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaService {
    ReservaDTO save(ReservaDTO dto);
    ReservaDTO update(ReservaDTO dto);
    Optional<ReservaDTO> partialUpdate(ReservaDTO dto);
    Page<ReservaDTO> findAll(Pageable pageable);
    Optional<ReservaDTO> findOne(Long id);
    void delete(Long id);

    long expirarReservasVencidas();
    ReservaDTO cancelarReserva(Long id, String motivo);
    Optional<ReservaDTO> findActivaByInventarioId(Long inventarioId);
}
