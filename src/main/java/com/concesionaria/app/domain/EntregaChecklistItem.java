package com.concesionaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "entrega_checklist_item")
public class EntregaChecklistItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrega_unidad_id", nullable = false)
    private EntregaUnidad entregaUnidad;

    @NotNull
    @Size(max = 50)
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @NotNull
    @Size(max = 200)
    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @NotNull
    @Column(name = "obligatorio", nullable = false)
    private Boolean obligatorio;

    @NotNull
    @Column(name = "completado", nullable = false)
    private Boolean completado;

    @Size(max = 255)
    @Column(name = "observaciones", length = 255)
    private String observaciones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntregaUnidad getEntregaUnidad() {
        return entregaUnidad;
    }

    public void setEntregaUnidad(EntregaUnidad entregaUnidad) {
        this.entregaUnidad = entregaUnidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getObligatorio() {
        return obligatorio;
    }

    public void setObligatorio(Boolean obligatorio) {
        this.obligatorio = obligatorio;
    }

    public Boolean getCompletado() {
        return completado;
    }

    public void setCompletado(Boolean completado) {
        this.completado = completado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}

