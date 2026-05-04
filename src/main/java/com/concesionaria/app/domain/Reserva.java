package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoReserva;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reserva")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Reserva implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "fecha_reserva", nullable = false)
    private Instant fechaReserva;

    @NotNull
    @Column(name = "fecha_vencimiento", nullable = false)
    private Instant fechaVencimiento;

    @DecimalMin(value = "0")
    @Column(name = "monto_senia", precision = 21, scale = 2)
    private BigDecimal montoSenia;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoReserva estado;

    @Size(max = 500)
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Size(max = 50)
    @Column(name = "usuario_creacion", length = 50)
    private String usuarioCreacion;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moneda_id")
    private Moneda moneda;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(Instant fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public Instant getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Instant fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getMontoSenia() {
        return montoSenia;
    }

    public void setMontoSenia(BigDecimal montoSenia) {
        this.montoSenia = montoSenia;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
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

    public Inventario getInventario() {
        return inventario;
    }

    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }
}
