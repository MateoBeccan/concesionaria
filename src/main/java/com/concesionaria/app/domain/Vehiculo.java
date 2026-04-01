package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.CondicionVehiculo;
import com.concesionaria.app.domain.enumeration.EstadoVehiculo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A Vehiculo.
 */
@Entity
@Table(name = "vehiculo")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Vehiculo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoVehiculo estado;

    @NotNull
    @Column(name = "fecha_fabricacion", nullable = false)
    private LocalDate fechaFabricacion;

    @NotNull
    @Min(value = 0)
    @Max(value = 1000000)
    @Column(name = "km", nullable = false)
    private Integer km;

    @NotNull
    @Pattern(regexp = "(^[A-Z]{3}[0-9]{3}$)|(^[A-Z]{2}[0-9]{3}[A-Z]{2}$)")
    @Column(name = "patente", nullable = false)
    private String patente;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "precio", precision = 21, scale = 2, nullable = false)
    private BigDecimal precio;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "modelo" }, allowSetters = true)
    private Version version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "combustible", "tipoCaja", "traccion" }, allowSetters = true)
    private Motor motor;

    @ManyToOne(fetch = FetchType.LAZY)
    private TipoVehiculo tipoVehiculo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "condicion", nullable = false)
    private CondicionVehiculo condicion;

    @JsonIgnoreProperties(value = { "vehiculo", "clienteReserva" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "vehiculo")
    private Inventario inventario;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Vehiculo id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EstadoVehiculo getEstado() {
        return this.estado;
    }

    public Vehiculo estado(EstadoVehiculo estado) {
        this.setEstado(estado);
        return this;
    }

    public CondicionVehiculo getCondicion() {
        return condicion;
    }

    public void setCondicion(CondicionVehiculo condicion) {
        this.condicion = condicion;
    }

    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    public LocalDate getFechaFabricacion() {
        return this.fechaFabricacion;
    }

    public Vehiculo fechaFabricacion(LocalDate fechaFabricacion) {
        this.setFechaFabricacion(fechaFabricacion);
        return this;
    }

    public void setFechaFabricacion(LocalDate fechaFabricacion) {
        this.fechaFabricacion = fechaFabricacion;
    }

    public Integer getKm() {
        return this.km;
    }

    public Vehiculo km(Integer km) {
        this.setKm(km);
        return this;
    }

    public void setKm(Integer km) {
        this.km = km;
    }

    public String getPatente() {
        return this.patente;
    }

    public Vehiculo patente(String patente) {
        this.setPatente(patente);
        return this;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public BigDecimal getPrecio() {
        return this.precio;
    }

    public Vehiculo precio(BigDecimal precio) {
        this.setPrecio(precio);
        return this;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Vehiculo createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public Vehiculo lastModifiedDate(Instant lastModifiedDate) {
        this.setLastModifiedDate(lastModifiedDate);
        return this;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Version getVersion() {
        return this.version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Vehiculo version(Version version) {
        this.setVersion(version);
        return this;
    }

    public Motor getMotor() {
        return this.motor;
    }

    public void setMotor(Motor motor) {
        this.motor = motor;
    }

    public Vehiculo motor(Motor motor) {
        this.setMotor(motor);
        return this;
    }

    public TipoVehiculo getTipoVehiculo() {
        return this.tipoVehiculo;
    }

    public void setTipoVehiculo(TipoVehiculo tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public Vehiculo tipoVehiculo(TipoVehiculo tipoVehiculo) {
        this.setTipoVehiculo(tipoVehiculo);
        return this;
    }

    public Inventario getInventario() {
        return this.inventario;
    }

    public void setInventario(Inventario inventario) {
        if (this.inventario != null) {
            this.inventario.setVehiculo(null);
        }
        if (inventario != null) {
            inventario.setVehiculo(this);
        }
        this.inventario = inventario;
    }

    public Vehiculo inventario(Inventario inventario) {
        this.setInventario(inventario);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vehiculo)) {
            return false;
        }
        return getId() != null && getId().equals(((Vehiculo) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Vehiculo{" +
            "id=" + getId() +
            ", estado='" + getEstado() + "'" +
            ", fechaFabricacion='" + getFechaFabricacion() + "'" +
            ", km=" + getKm() +
            ", patente='" + getPatente() + "'" +
            ", precio=" + getPrecio() +
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            "}";
    }
}
