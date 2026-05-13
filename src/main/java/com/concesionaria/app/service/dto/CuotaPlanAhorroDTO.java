package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class CuotaPlanAhorroDTO implements Serializable {

    private Long id;
    private Long contratoId;
    private Integer numeroCuota;
    private Instant fechaVencimiento;
    private BigDecimal importe;
    private EstadoCuotaPlanAhorro estado;
    private Instant fechaPago;
    private Long pagoId;
    private Long comprobanteId;
    private Long comprobantePlanAhorroId;
    private String numeroComprobantePlanAhorro;
    private EstadoComprobante estadoComprobantePlanAhorro;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContratoId() {
        return contratoId;
    }

    public void setContratoId(Long contratoId) {
        this.contratoId = contratoId;
    }

    public Integer getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(Integer numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

    public Instant getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Instant fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public EstadoCuotaPlanAhorro getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuotaPlanAhorro estado) {
        this.estado = estado;
    }

    public Instant getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Instant fechaPago) {
        this.fechaPago = fechaPago;
    }

    public Long getPagoId() {
        return pagoId;
    }

    public void setPagoId(Long pagoId) {
        this.pagoId = pagoId;
    }

    public Long getComprobanteId() {
        return comprobanteId;
    }

    public void setComprobanteId(Long comprobanteId) {
        this.comprobanteId = comprobanteId;
    }

    public Long getComprobantePlanAhorroId() {
        return comprobantePlanAhorroId;
    }

    public void setComprobantePlanAhorroId(Long comprobantePlanAhorroId) {
        this.comprobantePlanAhorroId = comprobantePlanAhorroId;
    }

    public String getNumeroComprobantePlanAhorro() {
        return numeroComprobantePlanAhorro;
    }

    public void setNumeroComprobantePlanAhorro(String numeroComprobantePlanAhorro) {
        this.numeroComprobantePlanAhorro = numeroComprobantePlanAhorro;
    }

    public EstadoComprobante getEstadoComprobantePlanAhorro() {
        return estadoComprobantePlanAhorro;
    }

    public void setEstadoComprobantePlanAhorro(EstadoComprobante estadoComprobantePlanAhorro) {
        this.estadoComprobantePlanAhorro = estadoComprobantePlanAhorro;
    }
}

