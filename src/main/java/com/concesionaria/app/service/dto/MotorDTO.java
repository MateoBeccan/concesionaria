package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Motor} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MotorDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String nombre;

    @NotNull
    @Min(value = 50)
    @Max(value = 10000)
    private Integer cilindradaCc;

    @NotNull
    @Min(value = 1)
    @Max(value = 16)
    private Integer cilindroCant;

    @NotNull
    @Min(value = 1)
    @Max(value = 2000)
    private Integer potenciaHp;

    @NotNull
    private Boolean turbo;

    private CombustibleDTO combustible;

    private TipoCajaDTO tipoCaja;

    private TraccionDTO traccion;

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

    public CombustibleDTO getCombustible() {
        return combustible;
    }

    public void setCombustible(CombustibleDTO combustible) {
        this.combustible = combustible;
    }

    public TipoCajaDTO getTipoCaja() {
        return tipoCaja;
    }

    public void setTipoCaja(TipoCajaDTO tipoCaja) {
        this.tipoCaja = tipoCaja;
    }

    public TraccionDTO getTraccion() {
        return traccion;
    }

    public void setTraccion(TraccionDTO traccion) {
        this.traccion = traccion;
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
            ", combustible=" + getCombustible() +
            ", tipoCaja=" + getTipoCaja() +
            ", traccion=" + getTraccion() +
            "}";
    }
}
