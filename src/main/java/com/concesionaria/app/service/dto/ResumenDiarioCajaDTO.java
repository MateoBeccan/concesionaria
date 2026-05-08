package com.concesionaria.app.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResumenDiarioCajaDTO implements Serializable {

    private LocalDate fecha;
    private BigDecimal totalIngresos;
    private BigDecimal totalReversos;
    private BigDecimal neto;
    private List<ResumenMetodoCajaDTO> porMetodo = new ArrayList<>();

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public BigDecimal getTotalReversos() {
        return totalReversos;
    }

    public void setTotalReversos(BigDecimal totalReversos) {
        this.totalReversos = totalReversos;
    }

    public BigDecimal getNeto() {
        return neto;
    }

    public void setNeto(BigDecimal neto) {
        this.neto = neto;
    }

    public List<ResumenMetodoCajaDTO> getPorMetodo() {
        return porMetodo;
    }

    public void setPorMetodo(List<ResumenMetodoCajaDTO> porMetodo) {
        this.porMetodo = porMetodo;
    }
}
