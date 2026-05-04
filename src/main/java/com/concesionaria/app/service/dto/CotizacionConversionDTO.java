package com.concesionaria.app.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class CotizacionConversionDTO implements Serializable {

    private BigDecimal montoOriginal;
    private BigDecimal montoConvertido;
    private Long monedaOrigenId;
    private String monedaOrigenCodigo;
    private Long monedaDestinoId;
    private String monedaDestinoCodigo;
    private Instant fechaOperacion;
    private BigDecimal cotizacionAplicada;
    private Long cotizacionOrigenId;
    private Long cotizacionDestinoId;
    private Instant fechaCotizacionUsada;
    private String detalleReglaAplicada;

    public BigDecimal getMontoOriginal() {
        return montoOriginal;
    }

    public void setMontoOriginal(BigDecimal montoOriginal) {
        this.montoOriginal = montoOriginal;
    }

    public BigDecimal getMontoConvertido() {
        return montoConvertido;
    }

    public void setMontoConvertido(BigDecimal montoConvertido) {
        this.montoConvertido = montoConvertido;
    }

    public Long getMonedaOrigenId() {
        return monedaOrigenId;
    }

    public void setMonedaOrigenId(Long monedaOrigenId) {
        this.monedaOrigenId = monedaOrigenId;
    }

    public String getMonedaOrigenCodigo() {
        return monedaOrigenCodigo;
    }

    public void setMonedaOrigenCodigo(String monedaOrigenCodigo) {
        this.monedaOrigenCodigo = monedaOrigenCodigo;
    }

    public Long getMonedaDestinoId() {
        return monedaDestinoId;
    }

    public void setMonedaDestinoId(Long monedaDestinoId) {
        this.monedaDestinoId = monedaDestinoId;
    }

    public String getMonedaDestinoCodigo() {
        return monedaDestinoCodigo;
    }

    public void setMonedaDestinoCodigo(String monedaDestinoCodigo) {
        this.monedaDestinoCodigo = monedaDestinoCodigo;
    }

    public Instant getFechaOperacion() {
        return fechaOperacion;
    }

    public void setFechaOperacion(Instant fechaOperacion) {
        this.fechaOperacion = fechaOperacion;
    }

    public BigDecimal getCotizacionAplicada() {
        return cotizacionAplicada;
    }

    public void setCotizacionAplicada(BigDecimal cotizacionAplicada) {
        this.cotizacionAplicada = cotizacionAplicada;
    }

    public Long getCotizacionOrigenId() {
        return cotizacionOrigenId;
    }

    public void setCotizacionOrigenId(Long cotizacionOrigenId) {
        this.cotizacionOrigenId = cotizacionOrigenId;
    }

    public Long getCotizacionDestinoId() {
        return cotizacionDestinoId;
    }

    public void setCotizacionDestinoId(Long cotizacionDestinoId) {
        this.cotizacionDestinoId = cotizacionDestinoId;
    }

    public Instant getFechaCotizacionUsada() {
        return fechaCotizacionUsada;
    }

    public void setFechaCotizacionUsada(Instant fechaCotizacionUsada) {
        this.fechaCotizacionUsada = fechaCotizacionUsada;
    }

    public String getDetalleReglaAplicada() {
        return detalleReglaAplicada;
    }

    public void setDetalleReglaAplicada(String detalleReglaAplicada) {
        this.detalleReglaAplicada = detalleReglaAplicada;
    }
}
