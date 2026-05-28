package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.EntregaUnidad;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.service.exception.BadRequestException;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class EntregaUnidadInventarioSync {

    private final InventarioRepository inventarioRepository;

    public EntregaUnidadInventarioSync(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    public void marcarEntregado(EntregaUnidad entrega) {
        Inventario inventario = inventarioRepository
            .findById(entrega.getInventario().getId())
            .orElseThrow(() -> new BadRequestException("Inventario no encontrado"));
        inventario.setEstadoInventario(EstadoInventario.VENDIDO);
        inventario.setFechaEgreso(Instant.now());
        inventarioRepository.save(inventario);
    }
}

