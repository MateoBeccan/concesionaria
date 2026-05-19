package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoPlanAhorro;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

public class PlanAhorroDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 120)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    private VersionDTO versionObjetivo;

    @NotNull
    @Min(1)
    @Max(360)
    private Integer cantidadCuotas;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal valorMovil;

    @NotNull
    private MonedaDTO moneda;

    @NotNull
    private EstadoPlanAhorro estado;

    private ReglaAdjudicacionPlanDTO reglaAdjudicacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public VersionDTO getVersionObjetivo() {
        return versionObjetivo;
    }

    public void setVersionObjetivo(VersionDTO versionObjetivo) {
        this.versionObjetivo = versionObjetivo;
    }

    public Integer getCantidadCuotas() {
        return cantidadCuotas;
    }

    public void setCantidadCuotas(Integer cantidadCuotas) {
        this.cantidadCuotas = cantidadCuotas;
    }

    public BigDecimal getValorMovil() {
        return valorMovil;
    }

    public void setValorMovil(BigDecimal valorMovil) {
        this.valorMovil = valorMovil;
    }

    public MonedaDTO getMoneda() {
        return moneda;
    }

    public void setMoneda(MonedaDTO moneda) {
        this.moneda = moneda;
    }

    public EstadoPlanAhorro getEstado() {
        return estado;
    }

    public void setEstado(EstadoPlanAhorro estado) {
        this.estado = estado;
    }

    public ReglaAdjudicacionPlanDTO getReglaAdjudicacion() {
        return reglaAdjudicacion;
    }

    public void setReglaAdjudicacion(ReglaAdjudicacionPlanDTO reglaAdjudicacion) {
        this.reglaAdjudicacion = reglaAdjudicacion;
    }
}

