package com.concesionaria.app.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Prueba1} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Prueba1DTO implements Serializable {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Prueba1DTO)) {
            return false;
        }

        Prueba1DTO prueba1DTO = (Prueba1DTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, prueba1DTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Prueba1DTO{" +
            "id=" + getId() +
            "}";
    }
}
