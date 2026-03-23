package com.concesionaria.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Venta.
 */
@Entity
@Table(name = "venta")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Venta implements Serializable {

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
    @Column(name = "cotizacion", precision = 21, scale = 2, nullable = false)
    private BigDecimal cotizacion;

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

    @DecimalMin(value = "0")
    @Column(name = "porcentaje_impuesto", precision = 21, scale = 2)
    private BigDecimal porcentajeImpuesto;

    @DecimalMin(value = "0")
    @Column(name = "total_pagado", precision = 21, scale = 2)
    private BigDecimal totalPagado;

    @DecimalMin(value = "0")
    @Column(name = "saldo", precision = 21, scale = 2)
    private BigDecimal saldo;

    @Size(max = 500)
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "condicionIva" }, allowSetters = true)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    private EstadoVenta estadoVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    private Moneda moneda;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Venta id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getFecha() {
        return this.fecha;
    }

    public Venta fecha(Instant fecha) {
        this.setFecha(fecha);
        return this;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getCotizacion() {
        return this.cotizacion;
    }

    public Venta cotizacion(BigDecimal cotizacion) {
        this.setCotizacion(cotizacion);
        return this;
    }

    public void setCotizacion(BigDecimal cotizacion) {
        this.cotizacion = cotizacion;
    }

    public BigDecimal getImporteNeto() {
        return this.importeNeto;
    }

    public Venta importeNeto(BigDecimal importeNeto) {
        this.setImporteNeto(importeNeto);
        return this;
    }

    public void setImporteNeto(BigDecimal importeNeto) {
        this.importeNeto = importeNeto;
    }

    public BigDecimal getImpuesto() {
        return this.impuesto;
    }

    public Venta impuesto(BigDecimal impuesto) {
        this.setImpuesto(impuesto);
        return this;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public Venta total(BigDecimal total) {
        this.setTotal(total);
        return this;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPorcentajeImpuesto() {
        return this.porcentajeImpuesto;
    }

    public Venta porcentajeImpuesto(BigDecimal porcentajeImpuesto) {
        this.setPorcentajeImpuesto(porcentajeImpuesto);
        return this;
    }

    public void setPorcentajeImpuesto(BigDecimal porcentajeImpuesto) {
        this.porcentajeImpuesto = porcentajeImpuesto;
    }

    public BigDecimal getTotalPagado() {
        return this.totalPagado;
    }

    public Venta totalPagado(BigDecimal totalPagado) {
        this.setTotalPagado(totalPagado);
        return this;
    }

    public void setTotalPagado(BigDecimal totalPagado) {
        this.totalPagado = totalPagado;
    }

    public BigDecimal getSaldo() {
        return this.saldo;
    }

    public Venta saldo(BigDecimal saldo) {
        this.setSaldo(saldo);
        return this;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getObservaciones() {
        return this.observaciones;
    }

    public Venta observaciones(String observaciones) {
        this.setObservaciones(observaciones);
        return this;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Cliente getCliente() {
        return this.cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Venta cliente(Cliente cliente) {
        this.setCliente(cliente);
        return this;
    }

    public EstadoVenta getEstadoVenta() {
        return this.estadoVenta;
    }

    public void setEstadoVenta(EstadoVenta estadoVenta) {
        this.estadoVenta = estadoVenta;
    }

    public Venta estadoVenta(EstadoVenta estadoVenta) {
        this.setEstadoVenta(estadoVenta);
        return this;
    }

    public Moneda getMoneda() {
        return this.moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public Venta moneda(Moneda moneda) {
        this.setMoneda(moneda);
        return this;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Venta user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Venta)) {
            return false;
        }
        return getId() != null && getId().equals(((Venta) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Venta{" +
            "id=" + getId() +
            ", fecha='" + getFecha() + "'" +
            ", cotizacion=" + getCotizacion() +
            ", importeNeto=" + getImporteNeto() +
            ", impuesto=" + getImpuesto() +
            ", total=" + getTotal() +
            ", porcentajeImpuesto=" + getPorcentajeImpuesto() +
            ", totalPagado=" + getTotalPagado() +
            ", saldo=" + getSaldo() +
            ", observaciones='" + getObservaciones() + "'" +
            "}";
    }
}
