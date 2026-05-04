package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoVenta;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "venta_historial")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VentaHistorial implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoVenta estadoAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoVenta estadoNuevo;

    @NotNull
    @Size(max = 100)
    @Column(name = "accion", nullable = false, length = 100)
    private String accion;

    @Size(max = 500)
    @Column(name = "detalle", length = 500)
    private String detalle;

    @Size(max = 50)
    @Column(name = "usuario", length = 50)
    private String usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public EstadoVenta getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoVenta estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoVenta getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoVenta estadoNuevo) {
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

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
