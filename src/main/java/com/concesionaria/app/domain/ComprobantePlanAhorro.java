package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "comprobante_plan_ahorro")
public class ComprobantePlanAhorro implements Serializable {

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
    @JoinColumn(name = "cuota_plan_ahorro_id", nullable = false)
    private CuotaPlanAhorro cuotaPlanAhorro;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    @NotNull
    @Size(max = 50)
    @Column(name = "numero_comprobante", length = 50, nullable = false)
    private String numeroComprobante;

    @NotNull
    @Column(name = "fecha_emision", nullable = false)
    private Instant fechaEmision;

    @NotNull
    @DecimalMin("0")
    @Column(name = "importe", precision = 21, scale = 2, nullable = false)
    private BigDecimal importe;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moneda_id", nullable = false)
    private Moneda moneda;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoComprobante estado;

    @Size(max = 50)
    @Column(name = "usuario_emision", length = 50)
    private String usuarioEmision;

    @Size(max = 500)
    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @Column(name = "fecha_anulacion")
    private Instant fechaAnulacion;

    @Size(max = 50)
    @Column(name = "usuario_anulacion", length = 50)
    private String usuarioAnulacion;

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

    public CuotaPlanAhorro getCuotaPlanAhorro() {
        return cuotaPlanAhorro;
    }

    public void setCuotaPlanAhorro(CuotaPlanAhorro cuotaPlanAhorro) {
        this.cuotaPlanAhorro = cuotaPlanAhorro;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public Instant getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Instant fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public EstadoComprobante getEstado() {
        return estado;
    }

    public void setEstado(EstadoComprobante estado) {
        this.estado = estado;
    }

    public String getUsuarioEmision() {
        return usuarioEmision;
    }

    public void setUsuarioEmision(String usuarioEmision) {
        this.usuarioEmision = usuarioEmision;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public Instant getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(Instant fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public String getUsuarioAnulacion() {
        return usuarioAnulacion;
    }

    public void setUsuarioAnulacion(String usuarioAnulacion) {
        this.usuarioAnulacion = usuarioAnulacion;
    }
}

