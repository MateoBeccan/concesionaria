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

    // ======================
    // RELACION CON MODELO
    // ======================
    @ManyToMany
    @JoinTable(
        name = "rel_version__modelo",
        joinColumns = @JoinColumn(name = "version_id"),
        inverseJoinColumns = @JoinColumn(name = "modelo_id")
    )
    @JsonIgnoreProperties(value = { "marca", "versioneses" }, allowSetters = true)
    private Set<Modelo> modeloses = new HashSet<>();

    // ======================
    // RELACION CON MOTOR
    // ======================
    @ManyToMany
    @JoinTable(
        name = "rel_version__motor",
        joinColumns = @JoinColumn(name = "version_id"),
        inverseJoinColumns = @JoinColumn(name = "motor_id")
    )
    @JsonIgnoreProperties(value = { "versioneses", "combustibleses" }, allowSetters = true)
    private Set<Motor> motoreses = new HashSet<>();

    // ======================
    // GETTERS / SETTERS
    // ======================

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

    // ======================
    // MODELOS
    // ======================

    public Set<Modelo> getModeloses() {
        return this.modeloses;
    }

    public void setModeloses(Set<Modelo> modelos) {
        this.modeloses = modelos;
    }

    public Version modeloses(Set<Modelo> modelos) {
        this.setModeloses(modelos);
        return this;
    }

    public Version addModelo(Modelo modelo) {
        this.modeloses.add(modelo);
        modelo.getVersioneses().add(this);
        return this;
    }

    public Version removeModelo(Modelo modelo) {
        this.modeloses.remove(modelo);
        modelo.getVersioneses().remove(this);
        return this;
    }

    // ======================
    // MOTORES
    // ======================

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

    public Version addMotor(Motor motor) {
        this.motoreses.add(motor);
        motor.getVersioneses().add(this);
        return this;
    }

    public Version removeMotor(Motor motor) {
        this.motoreses.remove(motor);
        motor.getVersioneses().remove(this);
        return this;
    }

    // ======================
    // EQUALS / HASH
    // ======================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        return getId() != null && getId().equals(((Version) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // ======================
    // TO STRING
    // ======================

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
