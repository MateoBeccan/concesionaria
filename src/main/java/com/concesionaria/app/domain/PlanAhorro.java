package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.EstadoPlanAhorro;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "plan_ahorro")
public class PlanAhorro implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 120)
    @Column(name = "nombre", length = 120, nullable = false)
    private String nombre;

    @Size(max = 500)
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id")
    private Version versionObjetivo;

    @NotNull
    @Min(1)
    @Max(360)
    @Column(name = "cantidad_cuotas", nullable = false)
    private Integer cantidadCuotas;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "valor_movil", precision = 21, scale = 2, nullable = false)
    private BigDecimal valorMovil;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moneda_id", nullable = false)
    private Moneda moneda;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoPlanAhorro estado;

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    private Set<ContratoPlanAhorro> contratos = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Version getVersionObjetivo() {
        return versionObjetivo;
    }

    public void setVersionObjetivo(Version versionObjetivo) {
        this.versionObjetivo = versionObjetivo;
    }

    public Integer getCantidadCuotas() {
        return cantidadCuotas;
    }

    public void setCantidadCuotas(Integer cantidadCuotas) {
        this.cantidadCuotas = cantidadCuotas;
    }

    public BigDecimal getValorMovil() {
        return valorMovil;
    }

    public void setValorMovil(BigDecimal valorMovil) {
        this.valorMovil = valorMovil;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public EstadoPlanAhorro getEstado() {
        return estado;
    }

    public void setEstado(EstadoPlanAhorro estado) {
        this.estado = estado;
    }

    public Set<ContratoPlanAhorro> getContratos() {
        return contratos;
    }

    public void setContratos(Set<ContratoPlanAhorro> contratos) {
        this.contratos = contratos;
    }
}

