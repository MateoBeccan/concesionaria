package com.concesionaria.app.service.dto;

import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public class PagoMultipleCuotasRequestDTO {

    @NotEmpty
    private List<Long> cuotaIds;

    private BigDecimal montoTotal;

    private String observaciones;

    private Long metodoPagoId;

    private Long monedaId;

    public List<Long> getCuotaIds() {
        return cuotaIds;
    }

    public void setCuotaIds(List<Long> cuotaIds) {
        this.cuotaIds = cuotaIds;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getMetodoPagoId() {
        return metodoPagoId;
    }

    public void setMetodoPagoId(Long metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }

    public Long getMonedaId() {
        return monedaId;
    }

    public void setMonedaId(Long monedaId) {
        this.monedaId = monedaId;
    }
}
