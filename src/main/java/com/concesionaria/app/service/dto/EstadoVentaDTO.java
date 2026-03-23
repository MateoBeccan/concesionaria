package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.EstadoVenta} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EstadoVentaDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 3, max = 50)
    private String codigo;

    @Size(max = 100)
    private String descripcion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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
        if (!(o instanceof EstadoVentaDTO)) {
            return false;
        }

        EstadoVentaDTO estadoVentaDTO = (EstadoVentaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, estadoVentaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EstadoVentaDTO{" +
            "id=" + getId() +
            ", codigo='" + getCodigo() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            "}";
    }
}
