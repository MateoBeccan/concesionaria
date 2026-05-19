package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.TipoReglaAdjudicacionPlan;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class ReglaAdjudicacionPlanDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 120)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    @NotNull
    private TipoReglaAdjudicacionPlan tipoRegla;

    private Integer minimoCuotas;

    private BigDecimal minimoPorcentaje;

    @NotNull
    private Boolean permiteMora;

    @NotNull
    private Boolean requiereContratoActivo;

    @NotNull
    private Boolean activo;

    private Instant createdDate;

    private Instant lastModifiedDate;

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

    public TipoReglaAdjudicacionPlan getTipoRegla() {
        return tipoRegla;
    }

    public void setTipoRegla(TipoReglaAdjudicacionPlan tipoRegla) {
        this.tipoRegla = tipoRegla;
    }

    public Integer getMinimoCuotas() {
        return minimoCuotas;
    }

    public void setMinimoCuotas(Integer minimoCuotas) {
        this.minimoCuotas = minimoCuotas;
    }

    public BigDecimal getMinimoPorcentaje() {
        return minimoPorcentaje;
    }

    public void setMinimoPorcentaje(BigDecimal minimoPorcentaje) {
        this.minimoPorcentaje = minimoPorcentaje;
    }

    public Boolean getPermiteMora() {
        return permiteMora;
    }

    public void setPermiteMora(Boolean permiteMora) {
        this.permiteMora = permiteMora;
    }

    public Boolean getRequiereContratoActivo() {
        return requiereContratoActivo;
    }

    public void setRequiereContratoActivo(Boolean requiereContratoActivo) {
        this.requiereContratoActivo = requiereContratoActivo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
