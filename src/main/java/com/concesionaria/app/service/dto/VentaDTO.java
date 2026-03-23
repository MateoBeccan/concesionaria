package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Venta} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VentaDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant fecha;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal cotizacion;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal importeNeto;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal impuesto;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal total;

    @DecimalMin(value = "0")
    private BigDecimal porcentajeImpuesto;

    @DecimalMin(value = "0")
    private BigDecimal totalPagado;

    @DecimalMin(value = "0")
    private BigDecimal saldo;

    @Size(max = 500)
    private String observaciones;

    private ClienteDTO cliente;

    private EstadoVentaDTO estadoVenta;

    private MonedaDTO moneda;

    private UserDTO user;

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

    public BigDecimal getCotizacion() {
        return cotizacion;
    }

    public void setCotizacion(BigDecimal cotizacion) {
        this.cotizacion = cotizacion;
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

    public BigDecimal getPorcentajeImpuesto() {
        return porcentajeImpuesto;
    }

    public void setPorcentajeImpuesto(BigDecimal porcentajeImpuesto) {
        this.porcentajeImpuesto = porcentajeImpuesto;
    }

    public BigDecimal getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(BigDecimal totalPagado) {
        this.totalPagado = totalPagado;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    public EstadoVentaDTO getEstadoVenta() {
        return estadoVenta;
    }

    public void setEstadoVenta(EstadoVentaDTO estadoVenta) {
        this.estadoVenta = estadoVenta;
    }

    public MonedaDTO getMoneda() {
        return moneda;
    }

    public void setMoneda(MonedaDTO moneda) {
        this.moneda = moneda;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VentaDTO)) {
            return false;
        }

        VentaDTO ventaDTO = (VentaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, ventaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VentaDTO{" +
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
            ", cliente=" + getCliente() +
            ", estadoVenta=" + getEstadoVenta() +
            ", moneda=" + getMoneda() +
            ", user=" + getUser() +
            "}";
    }
}
