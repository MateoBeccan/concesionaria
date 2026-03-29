package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Carroceria} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CarroceriaDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 3, max = 50)
    private String nombre;

    @Size(max = 100)
    private String descripcion;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CarroceriaDTO)) {
            return false;
        }

        CarroceriaDTO carroceriaDTO = (CarroceriaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, carroceriaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CarroceriaDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            "}";
    }
}
