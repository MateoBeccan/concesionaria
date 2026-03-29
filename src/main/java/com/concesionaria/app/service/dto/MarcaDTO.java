package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Marca} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MarcaDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String nombre;

    @Size(max = 100)
    private String paisOrigen;

    private Instant createdDate;

    private Instant lastModifiedDate;

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

    public String getPaisOrigen() {
        return paisOrigen;
    }

    public void setPaisOrigen(String paisOrigen) {
        this.paisOrigen = paisOrigen;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MarcaDTO)) {
            return false;
        }

        MarcaDTO marcaDTO = (MarcaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, marcaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MarcaDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", paisOrigen='" + getPaisOrigen() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            "}";
    }
}
