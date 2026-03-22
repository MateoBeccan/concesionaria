package com.concesionaria.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "motoreses")
    @JsonIgnoreProperties(value = { "motoreses", "modeloses" }, allowSetters = true)
    private Set<Version> versioneses = new HashSet<>();

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

    public Set<Version> getVersioneses() {
        return this.versioneses;
    }

    public void setVersioneses(Set<Version> versions) {
        if (this.versioneses != null) {
            this.versioneses.forEach(i -> i.removeMotores(this));
        }
        if (versions != null) {
            versions.forEach(i -> i.addMotores(this));
        }
        this.versioneses = versions;
    }

    public Motor versioneses(Set<Version> versions) {
        this.setVersioneses(versions);
        return this;
    }

    public Motor addVersiones(Version version) {
        this.versioneses.add(version);
        version.getMotoreses().add(this);
        return this;
    }

    public Motor removeVersiones(Version version) {
        this.versioneses.remove(version);
        version.getMotoreses().remove(this);
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
