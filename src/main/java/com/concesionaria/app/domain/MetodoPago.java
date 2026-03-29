package com.concesionaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;

/**
 * A MetodoPago.
 */
@Entity
@Table(name = "metodo_pago")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MetodoPago implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 2, max = 30)
    @Column(name = "codigo", length = 30, nullable = false)
    private String codigo;

    @Size(max = 100)
    @Column(name = "descripcion", length = 100)
    private String descripcion;

    @NotNull
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @NotNull
    @Column(name = "requiere_referencia", nullable = false)
    private Boolean requiereReferencia;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public MetodoPago id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public MetodoPago codigo(String codigo) {
        this.setCodigo(codigo);
        return this;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public MetodoPago descripcion(String descripcion) {
        this.setDescripcion(descripcion);
        return this;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getActivo() {
        return this.activo;
    }

    public MetodoPago activo(Boolean activo) {
        this.setActivo(activo);
        return this;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getRequiereReferencia() {
        return this.requiereReferencia;
    }

    public MetodoPago requiereReferencia(Boolean requiereReferencia) {
        this.setRequiereReferencia(requiereReferencia);
        return this;
    }

    public void setRequiereReferencia(Boolean requiereReferencia) {
        this.requiereReferencia = requiereReferencia;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetodoPago)) {
            return false;
        }
        return getId() != null && getId().equals(((MetodoPago) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MetodoPago{" +
            "id=" + getId() +
            ", codigo='" + getCodigo() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", activo='" + getActivo() + "'" +
            ", requiereReferencia='" + getRequiereReferencia() + "'" +
            "}";
    }
}
