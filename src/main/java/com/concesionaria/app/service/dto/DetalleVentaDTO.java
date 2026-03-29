package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.DetalleVenta} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DetalleVentaDTO implements Serializable {

    private Long id;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal precioUnitario;

    @NotNull
    @Min(value = 1)
    @Max(value = 1)
    private Integer cantidad;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal subtotal;

    private VentaDTO venta;

    private VehiculoDTO vehiculo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public VentaDTO getVenta() {
        return venta;
    }

    public void setVenta(VentaDTO venta) {
        this.venta = venta;
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
        if (!(o instanceof DetalleVentaDTO)) {
            return false;
        }

        DetalleVentaDTO detalleVentaDTO = (DetalleVentaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, detalleVentaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DetalleVentaDTO{" +
            "id=" + getId() +
            ", precioUnitario=" + getPrecioUnitario() +
            ", cantidad=" + getCantidad() +
            ", subtotal=" + getSubtotal() +
            ", venta=" + getVenta() +
            ", vehiculo=" + getVehiculo() +
            "}";
    }
}
