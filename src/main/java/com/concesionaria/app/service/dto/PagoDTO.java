package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Pago} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PagoDTO implements Serializable {

    private Long id;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal monto;

    @NotNull
    private Instant fecha;

    @Size(max = 100)
    private String referencia;

    private TipoMovimientoPago tipoMovimiento;

    @DecimalMin(value = "0")
    private BigDecimal cotizacionUsada;
    @DecimalMin(value = "0")
    private BigDecimal montoAplicadoVenta;
    private Instant fechaCotizacionUsada;
    private Long cotizacionId;
    private Long tasacionUsadoId;
    private TasacionUsadoDTO tasacionUsado;

    @Size(max = 50)
    private String usuarioRegistro;

    @Size(max = 500)
    private String observaciones;

    @Size(max = 100)
    private String comprobanteExterno;

    @Size(max = 100)
    private String bancoEntidad;

    @Size(max = 100)
    private String numeroOperacion;

    @Size(max = 500)
    private String motivoAnulacion;

    private Instant fechaAnulacion;

    @Size(max = 50)
    private String usuarioAnulacion;

    private Instant createdDate;

    private VentaDTO venta;

    private ReservaDTO reserva;

    private MetodoPagoDTO metodoPago;

    private MonedaDTO moneda;

    private EstadoPago estado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public String getReferencia() {
        return referencia;
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

    public Long getCotizacionId() {
        return cotizacionId;
    }

    public void setCotizacionId(Long cotizacionId) {
        this.cotizacionId = cotizacionId;
    }

    public Long getTasacionUsadoId() {
        return tasacionUsadoId;
    }

    public void setTasacionUsadoId(Long tasacionUsadoId) {
        this.tasacionUsadoId = tasacionUsadoId;
    }

    public TasacionUsadoDTO getTasacionUsado() {
        return tasacionUsado;
    }

    public void setTasacionUsado(TasacionUsadoDTO tasacionUsado) {
        this.tasacionUsado = tasacionUsado;
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
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public VentaDTO getVenta() {
        return venta;
    }

    public void setVenta(VentaDTO venta) {
        this.venta = venta;
    }

    public ReservaDTO getReserva() {
        return reserva;
    }

    public void setReserva(ReservaDTO reserva) {
        this.reserva = reserva;
    }

    public MetodoPagoDTO getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPagoDTO metodoPago) {
        this.metodoPago = metodoPago;
    }

    public MonedaDTO getMoneda() {
        return moneda;
    }

    public void setMoneda(MonedaDTO moneda) {
        this.moneda = moneda;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PagoDTO)) {
            return false;
        }

        PagoDTO pagoDTO = (PagoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, pagoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PagoDTO{" +
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
            ", venta=" + getVenta() +
            ", reserva=" + getReserva() +
            ", metodoPago=" + getMetodoPago() +
            ", moneda=" + getMoneda() +
            "}";
    }
}
