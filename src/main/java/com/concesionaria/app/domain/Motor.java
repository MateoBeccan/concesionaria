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
    @Size(min = 2, max = 100)
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @NotNull
    @Min(value = 50)
    @Max(value = 10000)
    @Column(name = "cilindrada_cc", nullable = false)
    private Integer cilindradaCc;

    @NotNull
    @Min(value = 1)
    @Max(value = 16)
    @Column(name = "cilindro_cant", nullable = false)
    private Integer cilindroCant;

    @NotNull
    @Min(value = 1)
    @Max(value = 2000)
    @Column(name = "potencia_hp", nullable = false)
    private Integer potenciaHp;

    @NotNull
    @Column(name = "turbo", nullable = false)
    private Boolean turbo;

    @ManyToOne(fetch = FetchType.LAZY)
    private Combustible combustible;

    @ManyToOne(fetch = FetchType.LAZY)
    private TipoCaja tipoCaja;

    @ManyToOne(fetch = FetchType.LAZY)
    private Traccion traccion;

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

    public TipoCaja getTipoCaja() {
        return this.tipoCaja;
    }

    public void setTipoCaja(TipoCaja tipoCaja) {
        this.tipoCaja = tipoCaja;
    }

    public Motor tipoCaja(TipoCaja tipoCaja) {
        this.setTipoCaja(tipoCaja);
        return this;
    }

    public Traccion getTraccion() {
        return this.traccion;
    }

    public void setTraccion(Traccion traccion) {
        this.traccion = traccion;
    }

    public Motor traccion(Traccion traccion) {
        this.setTraccion(traccion);
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
        return getClass().hashCode();
    }

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
