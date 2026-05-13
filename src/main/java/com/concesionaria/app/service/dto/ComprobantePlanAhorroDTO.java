package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class ComprobantePlanAhorroDTO implements Serializable {

    private Long id;
    private Long contratoPlanAhorroId;
    private Long cuotaPlanAhorroId;
    private Long pagoId;
    private String numeroComprobante;
    private Instant fechaEmision;
    private BigDecimal importe;
    private MonedaDTO moneda;
    private EstadoComprobante estado;
    private String usuarioEmision;
    private String motivoAnulacion;
    private Instant fechaAnulacion;
    private String usuarioAnulacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContratoPlanAhorroId() {
        return contratoPlanAhorroId;
    }

    public void setContratoPlanAhorroId(Long contratoPlanAhorroId) {
        this.contratoPlanAhorroId = contratoPlanAhorroId;
    }

    public Long getCuotaPlanAhorroId() {
        return cuotaPlanAhorroId;
    }

    public void setCuotaPlanAhorroId(Long cuotaPlanAhorroId) {
        this.cuotaPlanAhorroId = cuotaPlanAhorroId;
    }

    public Long getPagoId() {
        return pagoId;
    }

    public void setPagoId(Long pagoId) {
        this.pagoId = pagoId;
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

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public MonedaDTO getMoneda() {
        return moneda;
    }

    public void setMoneda(MonedaDTO moneda) {
        this.moneda = moneda;
    }

    public EstadoComprobante getEstado() {
        return estado;
    }

    public void setEstado(EstadoComprobante estado) {
        this.estado = estado;
    }

    public String getUsuarioEmision() {
        return usuarioEmision;
    }

    public void setUsuarioEmision(String usuarioEmision) {
        this.usuarioEmision = usuarioEmision;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public Instant getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(Instant fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public String getUsuarioAnulacion() {
        return usuarioAnulacion;
    }

    public void setUsuarioAnulacion(String usuarioAnulacion) {
        this.usuarioAnulacion = usuarioAnulacion;
    }
}

