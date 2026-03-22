package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Combustible} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CombustibleDTO implements Serializable {

    private Long id;

    @NotNull
    private String nombre;

    private String descripcion;

    private MotorDTO motor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public MotorDTO getMotor() {
        return motor;
    }

    public void setMotor(MotorDTO motor) {
        this.motor = motor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CombustibleDTO)) {
            return false;
        }

        CombustibleDTO combustibleDTO = (CombustibleDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, combustibleDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CombustibleDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", motor=" + getMotor() +
            "}";
    }
}
