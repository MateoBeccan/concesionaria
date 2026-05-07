package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoOperativoDocumental;
import com.concesionaria.app.domain.enumeration.OrigenVehiculo;
import com.concesionaria.app.domain.enumeration.TipoTenenciaInventario;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Inventario} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InventarioDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant fechaIngreso;

    @Size(max = 30)
    private String codigoInternoStock;

    @DecimalMin(value = "0")
    private BigDecimal costoAdquisicion;

    private Instant fechaEgreso;

    @Size(max = 50)
    private String tipoOrigenIngreso;

    private OrigenVehiculo origenVehiculo;

    private TipoTenenciaInventario tipoTenencia;

    private EstadoOperativoDocumental estadoOperativoDocumental;

    @Size(max = 100)
    private String proveedorReferencia;

    @Size(max = 30)
    private String numeroInternoStock;

    private UbicacionStockDTO ubicacionStock;

    @NotNull
    private EstadoInventario estadoInventario;

    @Size(max = 255)
    private String observaciones;

    private Instant createdDate;

    private String createdBy;

    private Instant lastModifiedDate;

    private String lastModifiedBy;

    private VehiculoDTO vehiculo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getFechaIngreso() {
        return fechaIngreso;
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

    public BigDecimal getCostoAdquisicion() {
        return costoAdquisicion;
    }

    public void setCostoAdquisicion(BigDecimal costoAdquisicion) {
        this.costoAdquisicion = costoAdquisicion;
    }

    public Instant getFechaEgreso() {
        return fechaEgreso;
    }

    public void setFechaEgreso(Instant fechaEgreso) {
        this.fechaEgreso = fechaEgreso;
    }

    public String getTipoOrigenIngreso() {
        return tipoOrigenIngreso;
    }

    public void setTipoOrigenIngreso(String tipoOrigenIngreso) {
        this.tipoOrigenIngreso = tipoOrigenIngreso;
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

    public UbicacionStockDTO getUbicacionStock() {
        return ubicacionStock;
    }

    public void setUbicacionStock(UbicacionStockDTO ubicacionStock) {
        this.ubicacionStock = ubicacionStock;
    }

    public EstadoInventario getEstadoInventario() {
        return estadoInventario;
    }

    public void setEstadoInventario(EstadoInventario estadoInventario) {
        this.estadoInventario = estadoInventario;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public VehiculoDTO getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(VehiculoDTO vehiculo) {
        this.vehiculo = vehiculo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InventarioDTO)) {
            return false;
        }

        InventarioDTO inventarioDTO = (InventarioDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, inventarioDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InventarioDTO{" +
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
            ", estadoInventario='" + getEstadoInventario() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            ", lastModifiedBy='" + getLastModifiedBy() + "'" +
            ", vehiculo=" + getVehiculo() +
            "}";
    }
}
