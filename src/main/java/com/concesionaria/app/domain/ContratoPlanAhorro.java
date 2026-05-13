package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contrato_plan_ahorro")
public class ContratoPlanAhorro implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanAhorro plan;

    @NotNull
    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoContratoPlanAhorro estado;

    @NotNull
    @Size(max = 40)
    @Column(name = "numero_contrato", length = 40, nullable = false, unique = true)
    private String numeroContrato;

    @NotNull
    @Min(1)
    @Max(360)
    @Column(name = "cuotas_totales", nullable = false)
    private Integer cuotasTotales;

    @NotNull
    @Min(0)
    @Column(name = "cuotas_pagadas", nullable = false)
    private Integer cuotasPagadas;

    @NotNull
    @DecimalMin("0")
    @Column(name = "saldo_pendiente", precision = 21, scale = 2, nullable = false)
    private BigDecimal saldoPendiente;

    @Size(max = 500)
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Size(max = 50)
    @Column(name = "usuario_registro", length = 50)
    private String usuarioRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "contrato", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CuotaPlanAhorro> cuotas = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public PlanAhorro getPlan() {
        return plan;
    }

    public void setPlan(PlanAhorro plan) {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<CuotaPlanAhorro> getCuotas() {
        return cuotas;
    }

    public void setCuotas(Set<CuotaPlanAhorro> cuotas) {
        this.cuotas = cuotas;
    }
}

