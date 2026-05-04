package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoTasacionUsado;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tasacion_usado")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TasacionUsado implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "fecha_tasacion", nullable = false)
    private Instant fechaTasacion;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "monto_tasacion", nullable = false, precision = 21, scale = 2)
    private BigDecimal montoTasacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTasacionUsado estado;

    @Size(max = 120)
    @Column(name = "marca_modelo_usado", length = 120)
    private String marcaModeloUsado;

    @Size(max = 30)
    @Column(name = "patente_usado", length = 30)
    private String patenteUsado;

    @Size(max = 30)
    @Column(name = "vin_chasis_usado", length = 30)
    private String vinChasisUsado;

    @Min(value = 1900)
    @Max(value = 2100)
    @Column(name = "anio_usado")
    private Integer anioUsado;

    @Min(value = 0)
    @Column(name = "km_usado")
    private Integer kmUsado;

    @Size(max = 50)
    @Column(name = "color_usado", length = 50)
    private String colorUsado;

    @Size(max = 50)
    @Column(name = "usuario_tasador", length = 50)
    private String usuarioTasador;

    @Size(max = 500)
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_generado_id")
    private Inventario inventarioGenerado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id")
    private Version version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motor_id")
    private Motor motor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_vehiculo_id")
    private TipoVehiculo tipoVehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tasador_user_id")
    private User tasadorUser;

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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Inventario getInventarioGenerado() {
        return inventarioGenerado;
    }

    public void setInventarioGenerado(Inventario inventarioGenerado) {
        this.inventarioGenerado = inventarioGenerado;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Motor getMotor() {
        return motor;
    }

    public void setMotor(Motor motor) {
        this.motor = motor;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(TipoVehiculo tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public User getTasadorUser() {
        return tasadorUser;
    }

    public void setTasadorUser(User tasadorUser) {
        this.tasadorUser = tasadorUser;
    }
}
