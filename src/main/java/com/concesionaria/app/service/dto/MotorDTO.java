package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Motor} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MotorDTO implements Serializable {

    private Long id;

    @NotNull
    private String nombre;

    private Integer cilindradaCc;

    private Integer cilindroCant;

    private Integer potenciaHp;

    private Boolean turbo;

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

    public Integer getCilindradaCc() {
        return cilindradaCc;
    }

    public void setCilindradaCc(Integer cilindradaCc) {
        this.cilindradaCc = cilindradaCc;
    }

    public Integer getCilindroCant() {
        return cilindroCant;
    }

    public void setCilindroCant(Integer cilindroCant) {
        this.cilindroCant = cilindroCant;
    }

    public Integer getPotenciaHp() {
        return potenciaHp;
    }

    public void setPotenciaHp(Integer potenciaHp) {
        this.potenciaHp = potenciaHp;
    }

    public Boolean getTurbo() {
        return turbo;
    }

    public void setTurbo(Boolean turbo) {
        this.turbo = turbo;
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
        if (!(o instanceof MotorDTO)) {
            return false;
        }

        MotorDTO motorDTO = (MotorDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, motorDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MotorDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", cilindradaCc=" + getCilindradaCc() +
            ", cilindroCant=" + getCilindroCant() +
            ", potenciaHp=" + getPotenciaHp() +
            ", turbo='" + getTurbo() + "'" +
            ", versioneses=" + getVersioneses() +
            "}";
    }
}
