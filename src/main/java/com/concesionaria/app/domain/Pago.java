package com.concesionaria.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Pago.
 */
@Entity
@Table(name = "pago")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Pago implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "monto", precision = 21, scale = 2, nullable = false)
    private BigDecimal monto;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Size(max = 100)
    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(name = "created_date")
    private Instant createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "cliente", "estadoVenta", "moneda", "user" }, allowSetters = true)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    private MetodoPago metodoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    private Moneda moneda;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Pago id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMonto() {
        return this.monto;
    }

    public Pago monto(BigDecimal monto) {
        this.setMonto(monto);
        return this;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Instant getFecha() {
        return this.fecha;
    }

    public Pago fecha(Instant fecha) {
        this.setFecha(fecha);
        return this;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public String getReferencia() {
        return this.referencia;
    }

    public Pago referencia(String referencia) {
        this.setReferencia(referencia);
        return this;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Pago createdDate(Instant createdDate) {
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

    public Pago venta(Venta venta) {
        this.setVenta(venta);
        return this;
    }

    public MetodoPago getMetodoPago() {
        return this.metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Pago metodoPago(MetodoPago metodoPago) {
        this.setMetodoPago(metodoPago);
        return this;
    }

    public Moneda getMoneda() {
        return this.moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public Pago moneda(Moneda moneda) {
        this.setMoneda(moneda);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pago)) {
            return false;
        }
        return getId() != null && getId().equals(((Pago) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Pago{" +
            "id=" + getId() +
            ", monto=" + getMonto() +
            ", fecha='" + getFecha() + "'" +
            ", referencia='" + getReferencia() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            "}";
    }
}
