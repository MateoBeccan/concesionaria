package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Modelo} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ModeloDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String nombre;

    @NotNull
    @Min(value = 1950)
    @Max(value = 2100)
    private Integer anioLanzamiento;

    private Instant createdDate;

    private Instant lastModifiedDate;

    private MarcaDTO marca;

    private CarroceriaDTO carroceria;

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

    public MarcaDTO getMarca() {
        return marca;
    }

    public void setMarca(MarcaDTO marca) {
        this.marca = marca;
    }

    public CarroceriaDTO getCarroceria() {
        return carroceria;
    }

    public void setCarroceria(CarroceriaDTO carroceria) {
        this.carroceria = carroceria;
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
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            ", marca=" + getMarca() +
            ", carroceria=" + getCarroceria() +
            "}";
    }
}
