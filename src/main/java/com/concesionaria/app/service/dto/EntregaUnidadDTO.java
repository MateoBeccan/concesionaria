package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoEntregaUnidad;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EntregaUnidadDTO implements Serializable {

    private Long id;
    private VentaDTO venta;
    private ClienteDTO cliente;
    private VehiculoDTO vehiculo;
    private InventarioDTO inventario;
    private Instant fechaProgramada;
    private Instant fechaEntrega;
    private EstadoEntregaUnidad estado;
    private String usuarioProgramacion;
    private String usuarioEntrega;
    private Integer kilometrajeEntrega;
    private String nivelCombustible;
    private String observaciones;
    private Instant createdDate;
    private Instant lastModifiedDate;
    private List<EntregaChecklistItemDTO> checklist = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VentaDTO getVenta() {
        return venta;
    }

    public void setVenta(VentaDTO venta) {
        this.venta = venta;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    public VehiculoDTO getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(VehiculoDTO vehiculo) {
        this.vehiculo = vehiculo;
    }

    public InventarioDTO getInventario() {
        return inventario;
    }

    public void setInventario(InventarioDTO inventario) {
        this.inventario = inventario;
    }

    public Instant getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(Instant fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public Instant getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(Instant fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public EstadoEntregaUnidad getEstado() {
        return estado;
    }

    public void setEstado(EstadoEntregaUnidad estado) {
        this.estado = estado;
    }

    public String getUsuarioProgramacion() {
        return usuarioProgramacion;
    }

    public void setUsuarioProgramacion(String usuarioProgramacion) {
        this.usuarioProgramacion = usuarioProgramacion;
    }

    public String getUsuarioEntrega() {
        return usuarioEntrega;
    }

    public void setUsuarioEntrega(String usuarioEntrega) {
        this.usuarioEntrega = usuarioEntrega;
    }

    public Integer getKilometrajeEntrega() {
        return kilometrajeEntrega;
    }

    public void setKilometrajeEntrega(Integer kilometrajeEntrega) {
        this.kilometrajeEntrega = kilometrajeEntrega;
    }

    public String getNivelCombustible() {
        return nivelCombustible;
    }

    public void setNivelCombustible(String nivelCombustible) {
        this.nivelCombustible = nivelCombustible;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public List<EntregaChecklistItemDTO> getChecklist() {
        return checklist;
    }

    public void setChecklist(List<EntregaChecklistItemDTO> checklist) {
        this.checklist = checklist;
    }
}

