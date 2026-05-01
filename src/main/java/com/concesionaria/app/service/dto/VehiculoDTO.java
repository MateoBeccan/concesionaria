package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVehiculo;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Vehiculo} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VehiculoDTO implements Serializable {

    private Long id;

    @NotNull
    private EstadoVehiculo estado;

    @NotNull
    private LocalDate fechaFabricacion;

    @NotNull
    @Min(value = 0)
    @Max(value = 1000000)
    private Integer km;

    @Size(max = 10)
    @Pattern(regexp = "(^[A-Z]{3}[0-9]{3}$)|(^[A-Z]{2}[0-9]{3}[A-Z]{2}$)")
    private String patente;

    @Size(max = 30)
    private String vinChasis;

    @Size(max = 50)
    private String color;

    @Size(max = 500)
    private String observaciones;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal precio;

    private Instant createdDate;

    private String createdBy;

    private Instant lastModifiedDate;

    private String lastModifiedBy;

    private VersionDTO version;

    private MotorDTO motor;

    private TipoVehiculoDTO tipoVehiculo;

    @NotNull
    private MonedaDTO moneda;

    private EstadoInventario estadoInventario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EstadoVehiculo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    public LocalDate getFechaFabricacion() {
        return fechaFabricacion;
    }

    public void setFechaFabricacion(LocalDate fechaFabricacion) {
        this.fechaFabricacion = fechaFabricacion;
    }

    public Integer getKm() {
        return km;
    }

    public void setKm(Integer km) {
        this.km = km;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public String getVinChasis() {
        return vinChasis;
    }

    public void setVinChasis(String vinChasis) {
        this.vinChasis = vinChasis;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public VersionDTO getVersion() {
        return version;
    }

    public void setVersion(VersionDTO version) {
        this.version = version;
    }

    public MotorDTO getMotor() {
        return motor;
    }

    public void setMotor(MotorDTO motor) {
        this.motor = motor;
    }

    public TipoVehiculoDTO getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(TipoVehiculoDTO tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public MonedaDTO getMoneda() {
        return moneda;
    }

    public void setMoneda(MonedaDTO moneda) {
        this.moneda = moneda;
    }

    public EstadoInventario getEstadoInventario() {
        return estadoInventario;
    }

    public void setEstadoInventario(EstadoInventario estadoInventario) {
        this.estadoInventario = estadoInventario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VehiculoDTO)) {
            return false;
        }

        VehiculoDTO vehiculoDTO = (VehiculoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, vehiculoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VehiculoDTO{" +
            "id=" + getId() +
            ", estado='" + getEstado() + "'" +
            ", fechaFabricacion='" + getFechaFabricacion() + "'" +
            ", km=" + getKm() +
            ", patente='" + getPatente() + "'" +
            ", vinChasis='" + getVinChasis() + "'" +
            ", color='" + getColor() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            ", precio=" + getPrecio() +
            ", createdDate='" + getCreatedDate() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            ", lastModifiedBy='" + getLastModifiedBy() + "'" +
            ", version=" + getVersion() +
            ", motor=" + getMotor() +
            ", tipoVehiculo=" + getTipoVehiculo() +
            ", moneda=" + getMoneda() +
            "}";
    }
}
