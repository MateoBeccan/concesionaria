package com.concesionaria.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Comprobante.
 */
@Entity
@Table(name = "comprobante")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Comprobante implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 3, max = 50)
    @Column(name = "numero_comprobante", length = 50, nullable = false)
    private String numeroComprobante;

    @NotNull
    @Column(name = "fecha_emision", nullable = false)
    private Instant fechaEmision;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "importe_neto", precision = 21, scale = 2, nullable = false)
    private BigDecimal importeNeto;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "impuesto", precision = 21, scale = 2, nullable = false)
    private BigDecimal impuesto;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "total", precision = 21, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "created_date")
    private Instant createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "cliente", "estado", "moneda", "user" }, allowSetters = true)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    private TipoComprobante tipoComprobante;

    @ManyToOne(fetch = FetchType.LAZY)
    private Moneda moneda;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Comprobante id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroComprobante() {
        return this.numeroComprobante;
    }

    public Comprobante numeroComprobante(String numeroComprobante) {
        this.setNumeroComprobante(numeroComprobante);
        return this;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public Instant getFechaEmision() {
        return this.fechaEmision;
    }

    public Comprobante fechaEmision(Instant fechaEmision) {
        this.setFechaEmision(fechaEmision);
        return this;
    }

    public void setFechaEmision(Instant fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public BigDecimal getImporteNeto() {
        return this.importeNeto;
    }

    public Comprobante importeNeto(BigDecimal importeNeto) {
        this.setImporteNeto(importeNeto);
        return this;
    }

    public void setImporteNeto(BigDecimal importeNeto) {
        this.importeNeto = importeNeto;
    }

    public BigDecimal getImpuesto() {
        return this.impuesto;
    }

    public Comprobante impuesto(BigDecimal impuesto) {
        this.setImpuesto(impuesto);
        return this;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public Comprobante total(BigDecimal total) {
        this.setTotal(total);
        return this;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Comprobante createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Venta getVenta() {
        return this.venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Comprobante venta(Venta venta) {
        this.setVenta(venta);
        return this;
    }

    public TipoComprobante getTipoComprobante() {
        return this.tipoComprobante;
    }

    public void setTipoComprobante(TipoComprobante tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public Comprobante tipoComprobante(TipoComprobante tipoComprobante) {
        this.setTipoComprobante(tipoComprobante);
        return this;
    }

    public Moneda getMoneda() {
        return this.moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public Comprobante moneda(Moneda moneda) {
        this.setMoneda(moneda);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Comprobante)) {
            return false;
        }
        return getId() != null && getId().equals(((Comprobante) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Comprobante{" +
            "id=" + getId() +
            ", numeroComprobante='" + getNumeroComprobante() + "'" +
            ", fechaEmision='" + getFechaEmision() + "'" +
            ", importeNeto=" + getImporteNeto() +
            ", impuesto=" + getImpuesto() +
            ", total=" + getTotal() +
            ", createdDate='" + getCreatedDate() + "'" +
            "}";
    }
}
