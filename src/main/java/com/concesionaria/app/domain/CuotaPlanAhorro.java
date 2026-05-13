package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cuota_plan_ahorro", uniqueConstraints = { @UniqueConstraint(name = "ux_cuota_plan_contrato_numero", columnNames = { "contrato_id", "numero_cuota" }) })
public class CuotaPlanAhorro implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private ContratoPlanAhorro contrato;

    @NotNull
    @Min(1)
    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @NotNull
    @Column(name = "fecha_vencimiento", nullable = false)
    private Instant fechaVencimiento;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "importe", precision = 21, scale = 2, nullable = false)
    private BigDecimal importe;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoCuotaPlanAhorro estado;

    @Column(name = "fecha_pago")
    private Instant fechaPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    private Pago pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContratoPlanAhorro getContrato() {
        return contrato;
    }

    public void setContrato(ContratoPlanAhorro contrato) {
        this.contrato = contrato;
    }

    public Integer getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(Integer numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

    public Instant getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Instant fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public EstadoCuotaPlanAhorro getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuotaPlanAhorro estado) {
        this.estado = estado;
    }

    public Instant getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Instant fechaPago) {
        this.fechaPago = fechaPago;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }
}

