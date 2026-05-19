package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.TipoReglaAdjudicacionPlan;
import java.io.Serializable;
import java.math.BigDecimal;

public class ElegibilidadAdjudicacionDTO implements Serializable {

    private boolean apto;
    private String mensaje;
    private TipoReglaAdjudicacionPlan tipoRegla;
    private Integer cuotasPagadas;
    private Integer minimoCuotas;
    private BigDecimal porcentajeIntegrado;
    private BigDecimal minimoPorcentaje;
    private Boolean permiteMora;
    private String nombreRegla;

    public boolean isApto() {
        return apto;
    }

    public void setApto(boolean apto) {
        this.apto = apto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public TipoReglaAdjudicacionPlan getTipoRegla() {
        return tipoRegla;
    }

    public void setTipoRegla(TipoReglaAdjudicacionPlan tipoRegla) {
        this.tipoRegla = tipoRegla;
    }

    public Integer getCuotasPagadas() {
        return cuotasPagadas;
    }

    public void setCuotasPagadas(Integer cuotasPagadas) {
        this.cuotasPagadas = cuotasPagadas;
    }

    public Integer getMinimoCuotas() {
        return minimoCuotas;
    }

    public void setMinimoCuotas(Integer minimoCuotas) {
        this.minimoCuotas = minimoCuotas;
    }

    public BigDecimal getPorcentajeIntegrado() {
        return porcentajeIntegrado;
    }

    public void setPorcentajeIntegrado(BigDecimal porcentajeIntegrado) {
        this.porcentajeIntegrado = porcentajeIntegrado;
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

    public String getNombreRegla() {
        return nombreRegla;
    }

    public void setNombreRegla(String nombreRegla) {
        this.nombreRegla = nombreRegla;
    }
}
