    package com.concesionaria.app.domain;

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

    @Size(max = 10)
    @Pattern(regexp = "(^[A-Z]{3}[0-9]{3}$)|(^[A-Z]{2}[0-9]{3}[A-Z]{2}$)")
    @Column(name = "patente", length = 10)
    private String patente;

    @Size(max = 30)
    @Column(name = "vin_chasis", length = 30)
    private String vinChasis;

    @Size(max = 50)
    @Column(name = "color", length = 50)
    private String color;

    @Size(max = 500)
    @Column(name = "observaciones", length = 500)
    private String observaciones;

        @NotNull
        @DecimalMin(value = "0.01")
        @Column(name = "precio", precision = 21, scale = 2, nullable = false)
        private BigDecimal precio;

        @Column(name = "created_date")
        private Instant createdDate;

        @Size(max = 50)
        @Column(name = "created_by", length = 50)
        private String createdBy;

        @Column(name = "last_modified_date")
        private Instant lastModifiedDate;

        @Size(max = 50)
        @Column(name = "last_modified_by", length = 50)
        private String lastModifiedBy;

        @ManyToOne(fetch = FetchType.LAZY)
        @JsonIgnoreProperties(value = { "modelo" }, allowSetters = true)
        private Version version;

        @ManyToOne(fetch = FetchType.LAZY)
        @JsonIgnoreProperties(value = { "combustible", "tipoCaja", "traccion" }, allowSetters = true)
        private Motor motor;

        @ManyToOne(fetch = FetchType.LAZY)
        private TipoVehiculo tipoVehiculo;

        @NotNull
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "moneda_id", nullable = false)
        private Moneda moneda;

    @JsonIgnoreProperties(value = { "vehiculo" }, allowSetters = true)
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

        public String getVinChasis() {
            return vinChasis;
        }

        public void setVinChasis(String vinChasis) {
            this.vinChasis = vinChasis;
        }

        public Vehiculo vinChasis(String vinChasis) {
            this.setVinChasis(vinChasis);
            return this;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public Vehiculo color(String color) {
            this.setColor(color);
            return this;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }

        public Vehiculo observaciones(String observaciones) {
            this.setObservaciones(observaciones);
            return this;
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

        public String getCreatedBy() {
            return this.createdBy;
        }

        public Vehiculo createdBy(String createdBy) {
            this.setCreatedBy(createdBy);
            return this;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public String getLastModifiedBy() {
            return this.lastModifiedBy;
        }

        public Vehiculo lastModifiedBy(String lastModifiedBy) {
            this.setLastModifiedBy(lastModifiedBy);
            return this;
        }

        public void setLastModifiedBy(String lastModifiedBy) {
            this.lastModifiedBy = lastModifiedBy;
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

        public Moneda getMoneda() {
            return this.moneda;
        }

        public void setMoneda(Moneda moneda) {
            this.moneda = moneda;
        }

        public Vehiculo moneda(Moneda moneda) {
            this.setMoneda(moneda);
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
                ", vinChasis='" + getVinChasis() + "'" +
                ", color='" + getColor() + "'" +
                ", observaciones='" + getObservaciones() + "'" +
                ", precio=" + getPrecio() +
                ", createdDate='" + getCreatedDate() + "'" +
                ", createdBy='" + getCreatedBy() + "'" +
                ", lastModifiedDate='" + getLastModifiedDate() + "'" +
                ", lastModifiedBy='" + getLastModifiedBy() + "'" +
                "}";
        }
    }
