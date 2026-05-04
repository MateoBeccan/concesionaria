package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.UbicacionStockDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UbicacionStockService {
    UbicacionStockDTO save(UbicacionStockDTO dto);
    UbicacionStockDTO update(UbicacionStockDTO dto);
    Optional<UbicacionStockDTO> partialUpdate(UbicacionStockDTO dto);
    Page<UbicacionStockDTO> findAll(Pageable pageable);
    Optional<UbicacionStockDTO> findOne(Long id);
    void delete(Long id);
}
