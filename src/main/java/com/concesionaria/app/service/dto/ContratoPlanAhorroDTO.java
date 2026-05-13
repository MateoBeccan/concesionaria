package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class ContratoPlanAhorroDTO implements Serializable {

    private Long id;
    @NotNull
    private ClienteDTO cliente;
    @NotNull
    private PlanAhorroDTO plan;
    @NotNull
    private Instant fechaInicio;
    @NotNull
    private EstadoContratoPlanAhorro estado;
    private String numeroContrato;
    private Integer cuotasTotales;
    private Integer cuotasPagadas;
    private BigDecimal saldoPendiente;
    @Size(max = 500)
    private String observaciones;
    private String usuarioRegistro;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    public PlanAhorroDTO getPlan() {
        return plan;
    }

    public void setPlan(PlanAhorroDTO plan) {
        this.plan = plan;
    }

    public Instant getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Instant fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public EstadoContratoPlanAhorro getEstado() {
        return estado;
    }

    public void setEstado(EstadoContratoPlanAhorro estado) {
        this.estado = estado;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    public Integer getCuotasTotales() {
        return cuotasTotales;
    }

    public void setCuotasTotales(Integer cuotasTotales) {
        this.cuotasTotales = cuotasTotales;
    }

    public Integer getCuotasPagadas() {
        return cuotasPagadas;
    }

    public void setCuotasPagadas(Integer cuotasPagadas) {
        this.cuotasPagadas = cuotasPagadas;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }
}

