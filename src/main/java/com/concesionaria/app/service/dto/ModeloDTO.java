package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.Carroceria;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Modelo} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ModeloDTO implements Serializable {

    private Long id;

    @NotNull
    private String nombre;

    private Integer anioLanzamiento;

    private Carroceria carroceria;

    private MarcaDTO marca;

    private Set<VersionDTO> versioneses = new HashSet<>();

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

    public Integer getAnioLanzamiento() {
        return anioLanzamiento;
    }

    public void setAnioLanzamiento(Integer anioLanzamiento) {
        this.anioLanzamiento = anioLanzamiento;
    }

    public Carroceria getCarroceria() {
        return carroceria;
    }

    public void setCarroceria(Carroceria carroceria) {
        this.carroceria = carroceria;
    }

    public MarcaDTO getMarca() {
        return marca;
    }

    public void setMarca(MarcaDTO marca) {
        this.marca = marca;
    }

    public Set<VersionDTO> getVersioneses() {
        return versioneses;
    }

    public void setVersioneses(Set<VersionDTO> versioneses) {
        this.versioneses = versioneses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModeloDTO)) {
            return false;
        }

        ModeloDTO modeloDTO = (ModeloDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, modeloDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ModeloDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", anioLanzamiento=" + getAnioLanzamiento() +
            ", carroceria='" + getCarroceria() + "'" +
            ", marca=" + getMarca() +
            ", versioneses=" + getVersioneses() +
            "}";
    }
}
