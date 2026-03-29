package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Comprobante} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ComprobanteDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 3, max = 50)
    private String numeroComprobante;

    @NotNull
    private Instant fechaEmision;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal importeNeto;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal impuesto;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal total;

    private Instant createdDate;

    private VentaDTO venta;

    private TipoComprobanteDTO tipoComprobante;

    private MonedaDTO moneda;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public Instant getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Instant fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public BigDecimal getImporteNeto() {
        return importeNeto;
    }

    public void setImporteNeto(BigDecimal importeNeto) {
        this.importeNeto = importeNeto;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public VentaDTO getVenta() {
        return venta;
    }

    public void setVenta(VentaDTO venta) {
        this.venta = venta;
    }

    public TipoComprobanteDTO getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(TipoComprobanteDTO tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
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
        if (!(o instanceof ComprobanteDTO)) {
            return false;
        }

        ComprobanteDTO comprobanteDTO = (ComprobanteDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, comprobanteDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ComprobanteDTO{" +
            "id=" + getId() +
            ", numeroComprobante='" + getNumeroComprobante() + "'" +
            ", fechaEmision='" + getFechaEmision() + "'" +
            ", importeNeto=" + getImporteNeto() +
            ", impuesto=" + getImpuesto() +
            ", total=" + getTotal() +
            ", createdDate='" + getCreatedDate() + "'" +
            ", venta=" + getVenta() +
            ", tipoComprobante=" + getTipoComprobante() +
            ", moneda=" + getMoneda() +
            "}";
    }
}
