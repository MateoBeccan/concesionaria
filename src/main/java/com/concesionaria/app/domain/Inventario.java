package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoOperativoDocumental;
import com.concesionaria.app.domain.enumeration.OrigenVehiculo;
import com.concesionaria.app.domain.enumeration.TipoTenenciaInventario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Inventario.
 */
@Entity
@Table(name = "inventario")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Inventario implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "fecha_ingreso", nullable = false)
    private Instant fechaIngreso;

    @NotNull
    @Size(max = 30)
    @Column(name = "codigo_interno_stock", length = 30, nullable = false, unique = true)
    private String codigoInternoStock;

    @DecimalMin(value = "0")
    @Column(name = "costo_adquisicion", precision = 21, scale = 2)
    private java.math.BigDecimal costoAdquisicion;

    @Column(name = "fecha_egreso")
    private Instant fechaEgreso;

    @Size(max = 50)
    @Column(name = "tipo_origen_ingreso", length = 50)
    private String tipoOrigenIngreso;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_vehiculo")
    private OrigenVehiculo origenVehiculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tenencia")
    private TipoTenenciaInventario tipoTenencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operativo_documental")
    private EstadoOperativoDocumental estadoOperativoDocumental;

    @Size(max = 100)
    @Column(name = "proveedor_referencia", length = 100)
    private String proveedorReferencia;

    @Size(max = 30)
    @Column(name = "numero_interno_stock", length = 30)
    private String numeroInternoStock;

    @Size(max = 100)
    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_stock_id")
    private UbicacionStock ubicacionStock;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_inventario", nullable = false)
    private EstadoInventario estadoInventario;

    @Size(max = 255)
    @Column(name = "observaciones", length = 255)
    private String observaciones;

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

    @JsonIgnoreProperties(value = { "version", "motor", "tipoVehiculo", "inventario" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Vehiculo vehiculo;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Inventario id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getFechaIngreso() {
        return this.fechaIngreso;
    }

    public Inventario fechaIngreso(Instant fechaIngreso) {
        this.setFechaIngreso(fechaIngreso);
        return this;
    }

    public void setFechaIngreso(Instant fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getCodigoInternoStock() {
        return codigoInternoStock;
    }

    public void setCodigoInternoStock(String codigoInternoStock) {
        this.codigoInternoStock = codigoInternoStock;
    }

    public Inventario codigoInternoStock(String codigoInternoStock) {
        this.setCodigoInternoStock(codigoInternoStock);
        return this;
    }

    public java.math.BigDecimal getCostoAdquisicion() {
        return costoAdquisicion;
    }

    public void setCostoAdquisicion(java.math.BigDecimal costoAdquisicion) {
        this.costoAdquisicion = costoAdquisicion;
    }

    public Inventario costoAdquisicion(java.math.BigDecimal costoAdquisicion) {
        this.setCostoAdquisicion(costoAdquisicion);
        return this;
    }

    public Instant getFechaEgreso() {
        return fechaEgreso;
    }

    public void setFechaEgreso(Instant fechaEgreso) {
        this.fechaEgreso = fechaEgreso;
    }

    public Inventario fechaEgreso(Instant fechaEgreso) {
        this.setFechaEgreso(fechaEgreso);
        return this;
    }

    public String getTipoOrigenIngreso() {
        return tipoOrigenIngreso;
    }

    public void setTipoOrigenIngreso(String tipoOrigenIngreso) {
        this.tipoOrigenIngreso = tipoOrigenIngreso;
    }

    public Inventario tipoOrigenIngreso(String tipoOrigenIngreso) {
        this.setTipoOrigenIngreso(tipoOrigenIngreso);
        return this;
    }

    public OrigenVehiculo getOrigenVehiculo() {
        return origenVehiculo;
    }

    public void setOrigenVehiculo(OrigenVehiculo origenVehiculo) {
        this.origenVehiculo = origenVehiculo;
    }

    public TipoTenenciaInventario getTipoTenencia() {
        return tipoTenencia;
    }

    public void setTipoTenencia(TipoTenenciaInventario tipoTenencia) {
        this.tipoTenencia = tipoTenencia;
    }

    public EstadoOperativoDocumental getEstadoOperativoDocumental() {
        return estadoOperativoDocumental;
    }

    public void setEstadoOperativoDocumental(EstadoOperativoDocumental estadoOperativoDocumental) {
        this.estadoOperativoDocumental = estadoOperativoDocumental;
    }

    public String getProveedorReferencia() {
        return proveedorReferencia;
    }

    public void setProveedorReferencia(String proveedorReferencia) {
        this.proveedorReferencia = proveedorReferencia;
    }

    public String getNumeroInternoStock() {
        return numeroInternoStock;
    }

    public void setNumeroInternoStock(String numeroInternoStock) {
        this.numeroInternoStock = numeroInternoStock;
    }

    public String getUbicacion() {
        return this.ubicacion;
    }

    public Inventario ubicacion(String ubicacion) {
        this.setUbicacion(ubicacion);
        return this;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public UbicacionStock getUbicacionStock() {
        return ubicacionStock;
    }

    public void setUbicacionStock(UbicacionStock ubicacionStock) {
        this.ubicacionStock = ubicacionStock;
    }

    public EstadoInventario getEstadoInventario() {
        return this.estadoInventario;
    }

    public Inventario estadoInventario(EstadoInventario estadoInventario) {
        this.setEstadoInventario(estadoInventario);
        return this;
    }

    public void setEstadoInventario(EstadoInventario estadoInventario) {
        this.estadoInventario = estadoInventario;
    }

    public String getObservaciones() {
        return this.observaciones;
    }

    public Inventario observaciones(String observaciones) {
        this.setObservaciones(observaciones);
        return this;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Inventario createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public Inventario lastModifiedDate(Instant lastModifiedDate) {
        this.setLastModifiedDate(lastModifiedDate);
        return this;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public Inventario createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public Inventario lastModifiedBy(String lastModifiedBy) {
        this.setLastModifiedBy(lastModifiedBy);
        return this;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Vehiculo getVehiculo() {
        return this.vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Inventario vehiculo(Vehiculo vehiculo) {
        this.setVehiculo(vehiculo);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Inventario)) {
            return false;
        }
        return getId() != null && getId().equals(((Inventario) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Inventario{" +
            "id=" + getId() +
            ", fechaIngreso='" + getFechaIngreso() + "'" +
            ", codigoInternoStock='" + getCodigoInternoStock() + "'" +
            ", costoAdquisicion='" + getCostoAdquisicion() + "'" +
            ", fechaEgreso='" + getFechaEgreso() + "'" +
            ", tipoOrigenIngreso='" + getTipoOrigenIngreso() + "'" +
            ", origenVehiculo='" + getOrigenVehiculo() + "'" +
            ", tipoTenencia='" + getTipoTenencia() + "'" +
            ", estadoOperativoDocumental='" + getEstadoOperativoDocumental() + "'" +
            ", proveedorReferencia='" + getProveedorReferencia() + "'" +
            ", numeroInternoStock='" + getNumeroInternoStock() + "'" +
            ", ubicacion='" + getUbicacion() + "'" +
            ", estadoInventario='" + getEstadoInventario() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            ", lastModifiedBy='" + getLastModifiedBy() + "'" +
            "}";
    }
}
