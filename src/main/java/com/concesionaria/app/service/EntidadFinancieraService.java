package com.concesionaria.app.service;

import com.concesionaria.app.service.dto.EntidadFinancieraDTO;
import java.util.List;

public interface EntidadFinancieraService {
    List<EntidadFinancieraDTO> findAllActivas();
}
