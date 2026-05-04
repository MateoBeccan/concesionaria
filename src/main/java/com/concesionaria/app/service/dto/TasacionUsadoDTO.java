package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.EstadoTasacionUsado;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class TasacionUsadoDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant fechaTasacion;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal montoTasacion;

    @NotNull
    private EstadoTasacionUsado estado;

    @Size(max = 120)
    private String marcaModeloUsado;

    @Size(max = 30)
    private String patenteUsado;

    @Size(max = 30)
    private String vinChasisUsado;

    @Min(1900)
    @Max(2100)
    private Integer anioUsado;

    @Min(0)
    private Integer kmUsado;

    @Size(max = 50)
    private String colorUsado;

    private String usuarioTasador;

    private String observaciones;

    private Instant createdDate;

    private Instant lastModifiedDate;

    private ClienteDTO cliente;

    private InventarioDTO inventarioGenerado;

    private VersionDTO version;

    private MotorDTO motor;

    private TipoVehiculoDTO tipoVehiculo;

    private UserDTO tasadorUser;

    private Long ventaAplicadaId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getFechaTasacion() {
        return fechaTasacion;
    }

    public void setFechaTasacion(Instant fechaTasacion) {
        this.fechaTasacion = fechaTasacion;
    }

    public BigDecimal getMontoTasacion() {
        return montoTasacion;
    }

    public void setMontoTasacion(BigDecimal montoTasacion) {
        this.montoTasacion = montoTasacion;
    }

    public EstadoTasacionUsado getEstado() {
        return estado;
    }

    public void setEstado(EstadoTasacionUsado estado) {
        this.estado = estado;
    }

    public String getMarcaModeloUsado() {
        return marcaModeloUsado;
    }

    public void setMarcaModeloUsado(String marcaModeloUsado) {
        this.marcaModeloUsado = marcaModeloUsado;
    }

    public String getPatenteUsado() {
        return patenteUsado;
    }

    public void setPatenteUsado(String patenteUsado) {
        this.patenteUsado = patenteUsado;
    }

    public String getVinChasisUsado() {
        return vinChasisUsado;
    }

    public void setVinChasisUsado(String vinChasisUsado) {
        this.vinChasisUsado = vinChasisUsado;
    }

    public String getUsuarioTasador() {
        return usuarioTasador;
    }

    public Integer getAnioUsado() {
        return anioUsado;
    }

    public void setAnioUsado(Integer anioUsado) {
        this.anioUsado = anioUsado;
    }

    public Integer getKmUsado() {
        return kmUsado;
    }

    public void setKmUsado(Integer kmUsado) {
        this.kmUsado = kmUsado;
    }

    public String getColorUsado() {
        return colorUsado;
    }

    public void setColorUsado(String colorUsado) {
        this.colorUsado = colorUsado;
    }

    public void setUsuarioTasador(String usuarioTasador) {
        this.usuarioTasador = usuarioTasador;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    public InventarioDTO getInventarioGenerado() {
        return inventarioGenerado;
    }

    public void setInventarioGenerado(InventarioDTO inventarioGenerado) {
        this.inventarioGenerado = inventarioGenerado;
    }

    public VersionDTO getVersion() {
        return version;
    }

    public void setVersion(VersionDTO version) {
        this.version = version;
    }

    public MotorDTO getMotor() {
        return motor;
    }

    public void setMotor(MotorDTO motor) {
        this.motor = motor;
    }

    public TipoVehiculoDTO getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(TipoVehiculoDTO tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public UserDTO getTasadorUser() {
        return tasadorUser;
    }

    public void setTasadorUser(UserDTO tasadorUser) {
        this.tasadorUser = tasadorUser;
    }

    public Long getVentaAplicadaId() {
        return ventaAplicadaId;
    }

    public void setVentaAplicadaId(Long ventaAplicadaId) {
        this.ventaAplicadaId = ventaAplicadaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TasacionUsadoDTO that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
