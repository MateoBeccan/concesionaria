package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Inventario.
 */
@Entity
@Table(name = "inventario")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Inventario implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "fecha_ingreso", nullable = false)
    private Instant fechaIngreso;

    @Size(max = 100)
    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_inventario", nullable = false)
    private EstadoInventario estadoInventario;

    @NotNull
    @Column(name = "disponible", nullable = false)
    private Boolean disponible;

    @Size(max = 255)
    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @Column(name = "created_date")
    private Instant createdDate;

    @Size(max = 50)
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @Size(max = 50)
    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "fecha_reserva")
    private Instant fechaReserva;

    @Column(name = "fecha_vencimiento_reserva")
    private Instant fechaVencimientoReserva;

    @JsonIgnoreProperties(value = { "version", "motor", "tipoVehiculo", "inventario" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "condicionIva", "tipoDocumento" }, allowSetters = true)
    private Cliente clienteReserva;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Inventario id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getFechaIngreso() {
        return this.fechaIngreso;
    }

    public Inventario fechaIngreso(Instant fechaIngreso) {
        this.setFechaIngreso(fechaIngreso);
        return this;
    }

    public void setFechaIngreso(Instant fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getUbicacion() {
        return this.ubicacion;
    }

    public Inventario ubicacion(String ubicacion) {
        this.setUbicacion(ubicacion);
        return this;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public EstadoInventario getEstadoInventario() {
        return this.estadoInventario;
    }

    public Inventario estadoInventario(EstadoInventario estadoInventario) {
        this.setEstadoInventario(estadoInventario);
        return this;
    }

    public void setEstadoInventario(EstadoInventario estadoInventario) {
        this.estadoInventario = estadoInventario;
    }

    public String getObservaciones() {
        return this.observaciones;
    }

    public Inventario observaciones(String observaciones) {
        this.setObservaciones(observaciones);
        return this;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Boolean getDisponible() {
        return this.disponible;
    }

    public Inventario disponible(Boolean disponible) {
        this.setDisponible(disponible);
        return this;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Inventario createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public Inventario lastModifiedDate(Instant lastModifiedDate) {
        this.setLastModifiedDate(lastModifiedDate);
        return this;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public Inventario createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public Inventario lastModifiedBy(String lastModifiedBy) {
        this.setLastModifiedBy(lastModifiedBy);
        return this;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getFechaReserva() {
        return this.fechaReserva;
    }

    public Inventario fechaReserva(Instant fechaReserva) {
        this.setFechaReserva(fechaReserva);
        return this;
    }

    public void setFechaReserva(Instant fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public Instant getFechaVencimientoReserva() {
        return this.fechaVencimientoReserva;
    }

    public Inventario fechaVencimientoReserva(Instant fechaVencimientoReserva) {
        this.setFechaVencimientoReserva(fechaVencimientoReserva);
        return this;
    }

    public void setFechaVencimientoReserva(Instant fechaVencimientoReserva) {
        this.fechaVencimientoReserva = fechaVencimientoReserva;
    }

    public Vehiculo getVehiculo() {
        return this.vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Inventario vehiculo(Vehiculo vehiculo) {
        this.setVehiculo(vehiculo);
        return this;
    }

    public Cliente getClienteReserva() {
        return this.clienteReserva;
    }

    public void setClienteReserva(Cliente cliente) {
        this.clienteReserva = cliente;
    }

    public Inventario clienteReserva(Cliente cliente) {
        this.setClienteReserva(cliente);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Inventario)) {
            return false;
        }
        return getId() != null && getId().equals(((Inventario) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Inventario{" +
            "id=" + getId() +
            ", fechaIngreso='" + getFechaIngreso() + "'" +
            ", ubicacion='" + getUbicacion() + "'" +
            ", estadoInventario='" + getEstadoInventario() + "'" +
            ", disponible='" + getDisponible() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            ", lastModifiedBy='" + getLastModifiedBy() + "'" +
            ", fechaReserva='" + getFechaReserva() + "'" +
            ", fechaVencimientoReserva='" + getFechaVencimientoReserva() + "'" +
            "}";
    }
}
