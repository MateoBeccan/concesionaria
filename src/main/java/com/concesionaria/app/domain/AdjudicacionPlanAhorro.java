package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoAdjudicacionPlanAhorro;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "adjudicacion_plan_ahorro")
public class AdjudicacionPlanAhorro implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_plan_ahorro_id", nullable = false)
    private ContratoPlanAhorro contratoPlanAhorro;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull
    @Column(name = "fecha_adjudicacion", nullable = false)
    private Instant fechaAdjudicacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoAdjudicacionPlanAhorro estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id")
    private Inventario inventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", unique = true)
    private Venta venta;

    @NotNull
    @DecimalMin("0")
    @Column(name = "monto_reconocido_cuotas", precision = 21, scale = 2, nullable = false)
    private BigDecimal montoReconocidoCuotas;

    @NotNull
    @DecimalMin("0")
    @Column(name = "diferencia_a_pagar", precision = 21, scale = 2, nullable = false)
    private BigDecimal diferenciaAPagar;

    @Size(max = 500)
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Size(max = 50)
    @Column(name = "usuario_adjudicacion", length = 50)
    private String usuarioAdjudicacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContratoPlanAhorro getContratoPlanAhorro() {
        return contratoPlanAhorro;
    }

    public void setContratoPlanAhorro(ContratoPlanAhorro contratoPlanAhorro) {
        this.contratoPlanAhorro = contratoPlanAhorro;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
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

    public Inventario getInventario() {
        return inventario;
    }

    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
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
