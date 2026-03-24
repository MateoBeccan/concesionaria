package com.concesionaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;

/**
 * A Motor.
 */
@Entity
@Table(name = "motor")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Motor implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "cilindrada_cc")
    private Integer cilindradaCc;

    @Column(name = "cilindro_cant")
    private Integer cilindroCant;

    @Column(name = "potencia_hp")
    private Integer potenciaHp;

    @Column(name = "turbo")
    private Boolean turbo;

    @ManyToOne(fetch = FetchType.LAZY)
    private Combustible combustible;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Motor id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Motor nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCilindradaCc() {
        return this.cilindradaCc;
    }

    public Motor cilindradaCc(Integer cilindradaCc) {
        this.setCilindradaCc(cilindradaCc);
        return this;
    }

    public void setCilindradaCc(Integer cilindradaCc) {
        this.cilindradaCc = cilindradaCc;
    }

    public Integer getCilindroCant() {
        return this.cilindroCant;
    }

    public Motor cilindroCant(Integer cilindroCant) {
        this.setCilindroCant(cilindroCant);
        return this;
    }

    public void setCilindroCant(Integer cilindroCant) {
        this.cilindroCant = cilindroCant;
    }

    public Integer getPotenciaHp() {
        return this.potenciaHp;
    }

    public Motor potenciaHp(Integer potenciaHp) {
        this.setPotenciaHp(potenciaHp);
        return this;
    }

    public void setPotenciaHp(Integer potenciaHp) {
        this.potenciaHp = potenciaHp;
    }

    public Boolean getTurbo() {
        return this.turbo;
    }

    public Motor turbo(Boolean turbo) {
        this.setTurbo(turbo);
        return this;
    }

    public void setTurbo(Boolean turbo) {
        this.turbo = turbo;
    }

    public Combustible getCombustible() {
        return this.combustible;
    }

    public void setCombustible(Combustible combustible) {
        this.combustible = combustible;
    }

    public Motor combustible(Combustible combustible) {
        this.setCombustible(combustible);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Motor)) {
            return false;
        }
        return getId() != null && getId().equals(((Motor) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Motor{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", cilindradaCc=" + getCilindradaCc() +
            ", cilindroCant=" + getCilindroCant() +
            ", potenciaHp=" + getPotenciaHp() +
            ", turbo='" + getTurbo() + "'" +
            "}";
    }
}
