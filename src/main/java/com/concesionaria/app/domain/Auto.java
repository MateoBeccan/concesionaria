package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.CondicionAuto;
import com.concesionaria.app.domain.enumeration.EstadoAuto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A Auto.
 */
@Entity
@Table(name = "auto")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Auto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoAuto estado;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "condicion", nullable = false)
    private CondicionAuto condicion;

    @Column(name = "fecha_fabricacion")
    private LocalDate fechaFabricacion;

    @Column(name = "km")
    private Integer km;

    @Column(name = "patente")
    private String patente;

    @Column(name = "precio", precision = 21, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY)
    private Marca marca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "marca", "versioneses" }, allowSetters = true)
    private Modelo modelo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "motoreses", "modeloses" }, allowSetters = true)
    private Version version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "versioneses" }, allowSetters = true)
    private Motor motor;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Auto id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EstadoAuto getEstado() {
        return this.estado;
    }

    public Auto estado(EstadoAuto estado) {
        this.setEstado(estado);
        return this;
    }

    public void setEstado(EstadoAuto estado) {
        this.estado = estado;
    }

    public CondicionAuto getCondicion() {
        return this.condicion;
    }

    public Auto condicion(CondicionAuto condicion) {
        this.setCondicion(condicion);
        return this;
    }

    public void setCondicion(CondicionAuto condicion) {
        this.condicion = condicion;
    }

    public LocalDate getFechaFabricacion() {
        return this.fechaFabricacion;
    }

    public Auto fechaFabricacion(LocalDate fechaFabricacion) {
        this.setFechaFabricacion(fechaFabricacion);
        return this;
    }

    public void setFechaFabricacion(LocalDate fechaFabricacion) {
        this.fechaFabricacion = fechaFabricacion;
    }

    public Integer getKm() {
        return this.km;
    }

    public Auto km(Integer km) {
        this.setKm(km);
        return this;
    }

    public void setKm(Integer km) {
        this.km = km;
    }

    public String getPatente() {
        return this.patente;
    }

    public Auto patente(String patente) {
        this.setPatente(patente);
        return this;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public BigDecimal getPrecio() {
        return this.precio;
    }

    public Auto precio(BigDecimal precio) {
        this.setPrecio(precio);
        return this;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Marca getMarca() {
        return this.marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Auto marca(Marca marca) {
        this.setMarca(marca);
        return this;
    }

    public Modelo getModelo() {
        return this.modelo;
    }

    public void setModelo(Modelo modelo) {
        this.modelo = modelo;
    }

    public Auto modelo(Modelo modelo) {
        this.setModelo(modelo);
        return this;
    }

    public Version getVersion() {
        return this.version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Auto version(Version version) {
        this.setVersion(version);
        return this;
    }

    public Motor getMotor() {
        return this.motor;
    }

    public void setMotor(Motor motor) {
        this.motor = motor;
    }

    public Auto motor(Motor motor) {
        this.setMotor(motor);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Auto)) {
            return false;
        }
        return getId() != null && getId().equals(((Auto) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Auto{" +
            "id=" + getId() +
            ", estado='" + getEstado() + "'" +
            ", condicion='" + getCondicion() + "'" +
            ", fechaFabricacion='" + getFechaFabricacion() + "'" +
            ", km=" + getKm() +
            ", patente='" + getPatente() + "'" +
            ", precio=" + getPrecio() +
            "}";
    }
}
