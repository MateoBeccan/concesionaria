package com.concesionaria.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Version.
 */
@Entity
@Table(name = "version")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Version implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "anio_inicio")
    private Integer anioInicio;

    @Column(name = "anio_fin")
    private Integer anioFin;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_version__motores",
        joinColumns = @JoinColumn(name = "version_id"),
        inverseJoinColumns = @JoinColumn(name = "motores_id")
    )
    @JsonIgnoreProperties(value = { "versioneses" }, allowSetters = true)
    private Set<Motor> motoreses = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "versioneses")
    @JsonIgnoreProperties(value = { "marca", "versioneses" }, allowSetters = true)
    private Set<Modelo> modeloses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Version id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Version nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public Version descripcion(String descripcion) {
        this.setDescripcion(descripcion);
        return this;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getAnioInicio() {
        return this.anioInicio;
    }

    public Version anioInicio(Integer anioInicio) {
        this.setAnioInicio(anioInicio);
        return this;
    }

    public void setAnioInicio(Integer anioInicio) {
        this.anioInicio = anioInicio;
    }

    public Integer getAnioFin() {
        return this.anioFin;
    }

    public Version anioFin(Integer anioFin) {
        this.setAnioFin(anioFin);
        return this;
    }

    public void setAnioFin(Integer anioFin) {
        this.anioFin = anioFin;
    }

    public Set<Motor> getMotoreses() {
        return this.motoreses;
    }

    public void setMotoreses(Set<Motor> motors) {
        this.motoreses = motors;
    }

    public Version motoreses(Set<Motor> motors) {
        this.setMotoreses(motors);
        return this;
    }

    public Version addMotores(Motor motor) {
        this.motoreses.add(motor);
        return this;
    }

    public Version removeMotores(Motor motor) {
        this.motoreses.remove(motor);
        return this;
    }

    public Set<Modelo> getModeloses() {
        return this.modeloses;
    }

    public void setModeloses(Set<Modelo> modelos) {
        if (this.modeloses != null) {
            this.modeloses.forEach(i -> i.removeVersiones(this));
        }
        if (modelos != null) {
            modelos.forEach(i -> i.addVersiones(this));
        }
        this.modeloses = modelos;
    }

    public Version modeloses(Set<Modelo> modelos) {
        this.setModeloses(modelos);
        return this;
    }

    public Version addModelos(Modelo modelo) {
        this.modeloses.add(modelo);
        modelo.getVersioneses().add(this);
        return this;
    }

    public Version removeModelos(Modelo modelo) {
        this.modeloses.remove(modelo);
        modelo.getVersioneses().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Version)) {
            return false;
        }
        return getId() != null && getId().equals(((Version) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Version{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", anioInicio=" + getAnioInicio() +
            ", anioFin=" + getAnioFin() +
            "}";
    }
}
