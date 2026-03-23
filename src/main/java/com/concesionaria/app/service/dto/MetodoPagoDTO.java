package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.MetodoPago} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MetodoPagoDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 2, max = 30)
    private String codigo;

    @Size(max = 100)
    private String descripcion;

    @NotNull
    private Boolean activo;

    private Boolean requiereReferencia;

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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getRequiereReferencia() {
        return requiereReferencia;
    }

    public void setRequiereReferencia(Boolean requiereReferencia) {
        this.requiereReferencia = requiereReferencia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetodoPagoDTO)) {
            return false;
        }

        MetodoPagoDTO metodoPagoDTO = (MetodoPagoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, metodoPagoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MetodoPagoDTO{" +
            "id=" + getId() +
            ", codigo='" + getCodigo() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", activo='" + getActivo() + "'" +
            ", requiereReferencia='" + getRequiereReferencia() + "'" +
            "}";
    }
}
