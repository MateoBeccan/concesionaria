package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.VentaHistorial;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.VentaHistorialRepository;
import java.time.Instant;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class VentaHistorialService {

    private final VentaHistorialRepository ventaHistorialRepository;

    public VentaHistorialService(VentaHistorialRepository ventaHistorialRepository) {
        this.ventaHistorialRepository = ventaHistorialRepository;
    }

    public void registrarCambioEstadoVentaSiCorresponde(
        Venta venta,
        EstadoVenta estadoAnterior,
        String accion,
        String detalle,
        Supplier<String> currentUserLoginSupplier
    ) {
        if (venta == null || venta.getId() == null || venta.getEstado() == null) {
            return;
        }
        if (estadoAnterior == venta.getEstado()) {
            return;
        }
        registrarHistorialVenta(venta, estadoAnterior, venta.getEstado(), accion, detalle, currentUserLoginSupplier);
    }

    public void registrarHistorialVenta(
        Venta venta,
        EstadoVenta estadoAnterior,
        EstadoVenta estadoNuevo,
        String accion,
        String detalle,
        Supplier<String> currentUserLoginSupplier
    ) {
        if (venta == null || venta.getId() == null || estadoNuevo == null) {
            return;
        }
        VentaHistorial historial = new VentaHistorial();
        historial.setVenta(venta);
        historial.setFecha(Instant.now());
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setAccion(accion);
        historial.setDetalle(detalle);
        historial.setUsuario(currentUserLoginSupplier.get());
        ventaHistorialRepository.save(historial);
    }
}

