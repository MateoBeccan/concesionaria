package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Pago.
 */
@Entity
@Table(name = "pago")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Pago implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(name = "monto", precision = 21, scale = 2, nullable = false)
    private BigDecimal monto;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Size(max = 100)
    @Column(name = "referencia", length = 100)
    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento")
    private TipoMovimientoPago tipoMovimiento;

    @DecimalMin(value = "0")
    @Column(name = "cotizacion_usada", precision = 21, scale = 8)
    private BigDecimal cotizacionUsada;

    @DecimalMin(value = "0")
    @Column(name = "monto_aplicado_venta", precision = 21, scale = 2)
    private BigDecimal montoAplicadoVenta;

    @Column(name = "fecha_cotizacion_usada")
    private Instant fechaCotizacionUsada;

    @Size(max = 50)
    @Column(name = "usuario_registro", length = 50)
    private String usuarioRegistro;

    @Size(max = 500)
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Size(max = 100)
    @Column(name = "comprobante_externo", length = 100)
    private String comprobanteExterno;

    @Size(max = 100)
    @Column(name = "banco_entidad", length = 100)
    private String bancoEntidad;

    @Size(max = 100)
    @Column(name = "numero_operacion", length = 100)
    private String numeroOperacion;

    @Size(max = 500)
    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @Column(name = "fecha_anulacion")
    private Instant fechaAnulacion;

    @Size(max = 50)
    @Column(name = "usuario_anulacion", length = 50)
    private String usuarioAnulacion;

    @Column(name = "created_date")
    private Instant createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "cliente", "estado", "moneda", "user" }, allowSetters = true)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "inventario", "cliente", "moneda" }, allowSetters = true)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    private MetodoPago metodoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    private Moneda moneda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacionRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tasacion_usado_id")
    private TasacionUsado tasacionUsado;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPago estado;

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public Pago estado(EstadoPago estado) {
        this.setEstado(estado);
        return this;
    }
// jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Pago id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMonto() {
        return this.monto;
    }

    public Pago monto(BigDecimal monto) {
        this.setMonto(monto);
        return this;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Instant getFecha() {
        return this.fecha;
    }

    public Pago fecha(Instant fecha) {
        this.setFecha(fecha);
        return this;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public String getReferencia() {
        return this.referencia;
    }

    public Pago referencia(String referencia) {
        this.setReferencia(referencia);
        return this;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public TipoMovimientoPago getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(TipoMovimientoPago tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public BigDecimal getCotizacionUsada() {
        return cotizacionUsada;
    }

    public void setCotizacionUsada(BigDecimal cotizacionUsada) {
        this.cotizacionUsada = cotizacionUsada;
    }

    public BigDecimal getMontoAplicadoVenta() {
        return montoAplicadoVenta;
    }

    public void setMontoAplicadoVenta(BigDecimal montoAplicadoVenta) {
        this.montoAplicadoVenta = montoAplicadoVenta;
    }

    public Instant getFechaCotizacionUsada() {
        return fechaCotizacionUsada;
    }

    public void setFechaCotizacionUsada(Instant fechaCotizacionUsada) {
        this.fechaCotizacionUsada = fechaCotizacionUsada;
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getComprobanteExterno() {
        return comprobanteExterno;
    }

    public void setComprobanteExterno(String comprobanteExterno) {
        this.comprobanteExterno = comprobanteExterno;
    }

    public String getBancoEntidad() {
        return bancoEntidad;
    }

    public void setBancoEntidad(String bancoEntidad) {
        this.bancoEntidad = bancoEntidad;
    }

    public String getNumeroOperacion() {
        return numeroOperacion;
    }

    public void setNumeroOperacion(String numeroOperacion) {
        this.numeroOperacion = numeroOperacion;
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

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Pago createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Venta getVenta() {
        return this.venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Pago venta(Venta venta) {
        this.setVenta(venta);
        return this;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Pago reserva(Reserva reserva) {
        this.setReserva(reserva);
        return this;
    }

    public MetodoPago getMetodoPago() {
        return this.metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Pago metodoPago(MetodoPago metodoPago) {
        this.setMetodoPago(metodoPago);
        return this;
    }

    public Moneda getMoneda() {
        return this.moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public Pago moneda(Moneda moneda) {
        this.setMoneda(moneda);
        return this;
    }

    public Cotizacion getCotizacionRef() {
        return cotizacionRef;
    }

    public void setCotizacionRef(Cotizacion cotizacionRef) {
        this.cotizacionRef = cotizacionRef;
    }

    public TasacionUsado getTasacionUsado() {
        return tasacionUsado;
    }

    public void setTasacionUsado(TasacionUsado tasacionUsado) {
        this.tasacionUsado = tasacionUsado;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pago)) {
            return false;
        }
        return getId() != null && getId().equals(((Pago) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Pago{" +
            "id=" + getId() +
            ", monto=" + getMonto() +
            ", fecha='" + getFecha() + "'" +
            ", referencia='" + getReferencia() + "'" +
            ", tipoMovimiento='" + getTipoMovimiento() + "'" +
            ", cotizacionUsada='" + getCotizacionUsada() + "'" +
            ", usuarioRegistro='" + getUsuarioRegistro() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            ", comprobanteExterno='" + getComprobanteExterno() + "'" +
            ", bancoEntidad='" + getBancoEntidad() + "'" +
            ", numeroOperacion='" + getNumeroOperacion() + "'" +
            ", motivoAnulacion='" + getMotivoAnulacion() + "'" +
            ", fechaAnulacion='" + getFechaAnulacion() + "'" +
            ", usuarioAnulacion='" + getUsuarioAnulacion() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", estado='" + getEstado() + "'" +
            "}";
    }
}
