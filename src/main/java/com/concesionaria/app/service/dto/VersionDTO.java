package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Version} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VersionDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    private String nombre;

    @Size(max = 150)
    private String descripcion;

    @NotNull
    @Min(value = 1950)
    @Max(value = 2100)
    private Integer anioInicio;

    @Min(value = 1950)
    @Max(value = 2100)
    private Integer anioFin;

    private ModeloDTO modelo;

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

    public Integer getAnioInicio() {
        return anioInicio;
    }

    public void setAnioInicio(Integer anioInicio) {
        this.anioInicio = anioInicio;
    }

    public Integer getAnioFin() {
        return anioFin;
    }

    public void setAnioFin(Integer anioFin) {
        this.anioFin = anioFin;
    }

    public ModeloDTO getModelo() {
        return modelo;
    }

    public void setModelo(ModeloDTO modelo) {
        this.modelo = modelo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VersionDTO)) {
            return false;
        }

        VersionDTO versionDTO = (VersionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, versionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VersionDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", anioInicio=" + getAnioInicio() +
            ", anioFin=" + getAnioFin() +
            ", modelo=" + getModelo() +
            "}";
    }
}
