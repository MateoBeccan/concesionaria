package com.concesionaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;

/**
 * A Modelo.
 */
@Entity
@Table(name = "modelo")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Modelo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "anio_lanzamiento")
    private Integer anioLanzamiento;

    @Column(name = "carroceria")
    private String carroceria;

    @ManyToOne(fetch = FetchType.LAZY)
    private Marca marca;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Modelo id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Modelo nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getAnioLanzamiento() {
        return this.anioLanzamiento;
    }

    public Modelo anioLanzamiento(Integer anioLanzamiento) {
        this.setAnioLanzamiento(anioLanzamiento);
        return this;
    }

    public void setAnioLanzamiento(Integer anioLanzamiento) {
        this.anioLanzamiento = anioLanzamiento;
    }

    public String getCarroceria() {
        return this.carroceria;
    }

    public Modelo carroceria(String carroceria) {
        this.setCarroceria(carroceria);
        return this;
    }

    public void setCarroceria(String carroceria) {
        this.carroceria = carroceria;
    }

    public Marca getMarca() {
        return this.marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Modelo marca(Marca marca) {
        this.setMarca(marca);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Modelo)) {
            return false;
        }
        return getId() != null && getId().equals(((Modelo) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Modelo{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", anioLanzamiento=" + getAnioLanzamiento() +
            ", carroceria='" + getCarroceria() + "'" +
            "}";
    }
}
