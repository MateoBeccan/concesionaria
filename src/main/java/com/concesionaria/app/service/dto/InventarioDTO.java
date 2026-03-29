package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoInventario;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Inventario} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InventarioDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant fechaIngreso;

    @Size(max = 100)
    private String ubicacion;

    @NotNull
    private EstadoInventario estadoInventario;

    @Size(max = 255)
    private String observaciones;

    @NotNull
    private Boolean disponible;

    private Instant createdDate;

    private Instant lastModifiedDate;

    private Instant fechaReserva;

    private Instant fechaVencimientoReserva;

    private VehiculoDTO vehiculo;

    private ClienteDTO clienteReserva;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Instant fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public EstadoInventario getEstadoInventario() {
        return estadoInventario;
    }

    public void setEstadoInventario(EstadoInventario estadoInventario) {
        this.estadoInventario = estadoInventario;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
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

    public Instant getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(Instant fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public Instant getFechaVencimientoReserva() {
        return fechaVencimientoReserva;
    }

    public void setFechaVencimientoReserva(Instant fechaVencimientoReserva) {
        this.fechaVencimientoReserva = fechaVencimientoReserva;
    }

    public VehiculoDTO getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(VehiculoDTO vehiculo) {
        this.vehiculo = vehiculo;
    }

    public ClienteDTO getClienteReserva() {
        return clienteReserva;
    }

    public void setClienteReserva(ClienteDTO clienteReserva) {
        this.clienteReserva = clienteReserva;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InventarioDTO)) {
            return false;
        }

        InventarioDTO inventarioDTO = (InventarioDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, inventarioDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InventarioDTO{" +
            "id=" + getId() +
            ", fechaIngreso='" + getFechaIngreso() + "'" +
            ", ubicacion='" + getUbicacion() + "'" +
            ", estadoInventario='" + getEstadoInventario() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            ", disponible='" + getDisponible() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            ", fechaReserva='" + getFechaReserva() + "'" +
            ", fechaVencimientoReserva='" + getFechaVencimientoReserva() + "'" +
            ", vehiculo=" + getVehiculo() +
            ", clienteReserva=" + getClienteReserva() +
            "}";
    }
}
