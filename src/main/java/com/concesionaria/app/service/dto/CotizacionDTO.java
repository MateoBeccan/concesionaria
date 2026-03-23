package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Cotizacion} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CotizacionDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant fecha;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal valorCompra;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal valorVenta;

    @NotNull
    private Boolean activo;

    private MonedaDTO moneda;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getValorCompra() {
        return valorCompra;
    }

    public void setValorCompra(BigDecimal valorCompra) {
        this.valorCompra = valorCompra;
    }

    public BigDecimal getValorVenta() {
        return valorVenta;
    }

    public void setValorVenta(BigDecimal valorVenta) {
        this.valorVenta = valorVenta;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public MonedaDTO getMoneda() {
        return moneda;
    }

    public void setMoneda(MonedaDTO moneda) {
        this.moneda = moneda;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CotizacionDTO)) {
            return false;
        }

        CotizacionDTO cotizacionDTO = (CotizacionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, cotizacionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CotizacionDTO{" +
            "id=" + getId() +
            ", fecha='" + getFecha() + "'" +
            ", valorCompra=" + getValorCompra() +
            ", valorVenta=" + getValorVenta() +
            ", activo='" + getActivo() + "'" +
            ", moneda=" + getMoneda() +
            "}";
    }
}
