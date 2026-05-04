package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoVenta;
import java.io.Serializable;
import java.time.Instant;

public class VentaHistorialDTO implements Serializable {

    private Long id;
    private Long ventaId;
    private Instant fecha;
    private EstadoVenta estadoAnterior;
    private EstadoVenta estadoNuevo;
    private String accion;
    private String detalle;
    private String usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public EstadoVenta getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoVenta estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoVenta getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoVenta estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
