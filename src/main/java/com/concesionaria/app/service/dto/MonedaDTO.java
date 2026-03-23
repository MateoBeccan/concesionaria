package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Moneda} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MonedaDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 2, max = 10)
    private String codigo;

    @Size(max = 50)
    private String descripcion;

    @Size(max = 5)
    private String simbolo;

    @NotNull
    private Boolean activo;

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

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MonedaDTO)) {
            return false;
        }

        MonedaDTO monedaDTO = (MonedaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, monedaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MonedaDTO{" +
            "id=" + getId() +
            ", codigo='" + getCodigo() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", simbolo='" + getSimbolo() + "'" +
            ", activo='" + getActivo() + "'" +
            "}";
    }
}
