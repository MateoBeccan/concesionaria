package com.concesionaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Cotizacion.
 */
@Entity
@Table(name = "cotizacion")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Cotizacion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "valor_compra", precision = 21, scale = 2, nullable = false)
    private BigDecimal valorCompra;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "valor_venta", precision = 21, scale = 2, nullable = false)
    private BigDecimal valorVenta;

    @NotNull
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @ManyToOne(fetch = FetchType.LAZY)
    private Moneda moneda;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Cotizacion id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getFecha() {
        return this.fecha;
    }

    public Cotizacion fecha(Instant fecha) {
        this.setFecha(fecha);
        return this;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getValorCompra() {
        return this.valorCompra;
    }

    public Cotizacion valorCompra(BigDecimal valorCompra) {
        this.setValorCompra(valorCompra);
        return this;
    }

    public void setValorCompra(BigDecimal valorCompra) {
        this.valorCompra = valorCompra;
    }

    public BigDecimal getValorVenta() {
        return this.valorVenta;
    }

    public Cotizacion valorVenta(BigDecimal valorVenta) {
        this.setValorVenta(valorVenta);
        return this;
    }

    public void setValorVenta(BigDecimal valorVenta) {
        this.valorVenta = valorVenta;
    }

    public Boolean getActivo() {
        return this.activo;
    }

    public Cotizacion activo(Boolean activo) {
        this.setActivo(activo);
        return this;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Moneda getMoneda() {
        return this.moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public Cotizacion moneda(Moneda moneda) {
        this.setMoneda(moneda);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cotizacion)) {
            return false;
        }
        return getId() != null && getId().equals(((Cotizacion) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Cotizacion{" +
            "id=" + getId() +
            ", fecha='" + getFecha() + "'" +
            ", valorCompra=" + getValorCompra() +
            ", valorVenta=" + getValorVenta() +
            ", activo='" + getActivo() + "'" +
            "}";
    }
}
