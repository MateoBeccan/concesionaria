package com.concesionaria.app.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ResumenMetodoCajaDTO implements Serializable {

    private Long metodoPagoId;
    private String metodoPagoCodigo;
    private String metodoPagoDescripcion;
    private BigDecimal totalIngresos;
    private BigDecimal totalReversos;
    private BigDecimal neto;

    public Long getMetodoPagoId() {
        return metodoPagoId;
    }

    public void setMetodoPagoId(Long metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }

    public String getMetodoPagoCodigo() {
        return metodoPagoCodigo;
    }

    public void setMetodoPagoCodigo(String metodoPagoCodigo) {
        this.metodoPagoCodigo = metodoPagoCodigo;
    }

    public String getMetodoPagoDescripcion() {
        return metodoPagoDescripcion;
    }

    public void setMetodoPagoDescripcion(String metodoPagoDescripcion) {
        this.metodoPagoDescripcion = metodoPagoDescripcion;
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
}
