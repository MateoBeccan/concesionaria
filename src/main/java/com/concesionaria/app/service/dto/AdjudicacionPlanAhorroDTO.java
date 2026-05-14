package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoAdjudicacionPlanAhorro;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class AdjudicacionPlanAhorroDTO implements Serializable {

    private Long id;
    private Long contratoPlanAhorroId;
    private String numeroContrato;
    private String planNombre;
    private ClienteDTO cliente;
    private Instant fechaAdjudicacion;
    private EstadoAdjudicacionPlanAhorro estado;
    private InventarioDTO inventario;
    private VehiculoDTO vehiculo;
    private VentaDTO venta;
    private BigDecimal montoReconocidoCuotas;
    private BigDecimal diferenciaAPagar;
    private String observaciones;
    private String usuarioAdjudicacion;

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

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    public String getPlanNombre() {
        return planNombre;
    }

    public void setPlanNombre(String planNombre) {
        this.planNombre = planNombre;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    public Instant getFechaAdjudicacion() {
        return fechaAdjudicacion;
    }

    public void setFechaAdjudicacion(Instant fechaAdjudicacion) {
        this.fechaAdjudicacion = fechaAdjudicacion;
    }

    public EstadoAdjudicacionPlanAhorro getEstado() {
        return estado;
    }

    public void setEstado(EstadoAdjudicacionPlanAhorro estado) {
        this.estado = estado;
    }

    public InventarioDTO getInventario() {
        return inventario;
    }

    public void setInventario(InventarioDTO inventario) {
        this.inventario = inventario;
    }

    public VehiculoDTO getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(VehiculoDTO vehiculo) {
        this.vehiculo = vehiculo;
    }

    public VentaDTO getVenta() {
        return venta;
    }

    public void setVenta(VentaDTO venta) {
        this.venta = venta;
    }

    public BigDecimal getMontoReconocidoCuotas() {
        return montoReconocidoCuotas;
    }

    public void setMontoReconocidoCuotas(BigDecimal montoReconocidoCuotas) {
        this.montoReconocidoCuotas = montoReconocidoCuotas;
    }

    public BigDecimal getDiferenciaAPagar() {
        return diferenciaAPagar;
    }

    public void setDiferenciaAPagar(BigDecimal diferenciaAPagar) {
        this.diferenciaAPagar = diferenciaAPagar;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getUsuarioAdjudicacion() {
        return usuarioAdjudicacion;
    }

    public void setUsuarioAdjudicacion(String usuarioAdjudicacion) {
        this.usuarioAdjudicacion = usuarioAdjudicacion;
    }
}
