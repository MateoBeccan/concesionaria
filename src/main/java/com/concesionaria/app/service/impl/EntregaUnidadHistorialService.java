package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.VentaHistorial;
import com.concesionaria.app.repository.VentaHistorialRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class EntregaUnidadHistorialService {

    private final VentaHistorialRepository ventaHistorialRepository;

    public EntregaUnidadHistorialService(VentaHistorialRepository ventaHistorialRepository) {
        this.ventaHistorialRepository = ventaHistorialRepository;
    }

    public void registrarHistorialEntrega(Venta venta, String accion, String detalle, String usuario) {
        VentaHistorial historial = new VentaHistorial();
        historial.setVenta(venta);
        historial.setFecha(Instant.now());
        historial.setEstadoAnterior(venta.getEstado());
        historial.setEstadoNuevo(venta.getEstado());
        historial.setAccion(accion);
        historial.setDetalle(detalle);
        historial.setUsuario(usuario);
        ventaHistorialRepository.save(historial);
    }
}

