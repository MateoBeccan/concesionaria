package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoInventario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "inventario_historial")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InventarioHistorial implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoInventario estadoAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoInventario estadoNuevo;

    @NotNull
    @Size(max = 100)
    @Column(name = "accion", length = 100, nullable = false)
    private String accion;

    @Size(max = 500)
    @Column(name = "detalle", length = 500)
    private String detalle;

    @Size(max = 255)
    @Column(name = "motivo", length = 255)
    private String motivo;

    @Size(max = 50)
    @Column(name = "usuario", length = 50)
    private String usuario;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    public EstadoInventario getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoInventario estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoInventario getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoInventario estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
