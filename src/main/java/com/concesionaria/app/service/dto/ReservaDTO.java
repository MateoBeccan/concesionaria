package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoReserva;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class ReservaDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant fechaReserva;

    @NotNull
    private Instant fechaVencimiento;

    @DecimalMin(value = "0")
    private BigDecimal montoSenia;

    @NotNull
    private EstadoReserva estado;

    @Size(max = 500)
    private String observaciones;

    private String usuarioCreacion;

    private Instant createdDate;

    private Instant lastModifiedDate;

    private InventarioDTO inventario;

    private ClienteDTO cliente;

    private VentaDTO ventaAsociada;

    private MonedaDTO moneda;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(Instant fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public Instant getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Instant fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getMontoSenia() {
        return montoSenia;
    }

    public void setMontoSenia(BigDecimal montoSenia) {
        this.montoSenia = montoSenia;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
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

    public InventarioDTO getInventario() {
        return inventario;
    }

    public void setInventario(InventarioDTO inventario) {
        this.inventario = inventario;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    public VentaDTO getVentaAsociada() {
        return ventaAsociada;
    }

    public void setVentaAsociada(VentaDTO ventaAsociada) {
        this.ventaAsociada = ventaAsociada;
    }

    public MonedaDTO getMoneda() {
        return moneda;
    }

    public void setMoneda(MonedaDTO moneda) {
        this.moneda = moneda;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservaDTO that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
