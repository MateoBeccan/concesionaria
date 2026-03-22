package com.concesionaria.app.service.criteria;

import com.concesionaria.app.domain.enumeration.CondicionAuto;
import com.concesionaria.app.domain.enumeration.EstadoAuto;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.concesionaria.app.domain.Auto} entity. This class is used
 * in {@link com.concesionaria.app.web.rest.AutoResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /autos?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AutoCriteria implements Serializable, Criteria {

    /**
     * Class for filtering EstadoAuto
     */
    public static class EstadoAutoFilter extends Filter<EstadoAuto> {

        public EstadoAutoFilter() {}

        public EstadoAutoFilter(EstadoAutoFilter filter) {
            super(filter);
        }

        @Override
        public EstadoAutoFilter copy() {
            return new EstadoAutoFilter(this);
        }
    }

    /**
     * Class for filtering CondicionAuto
     */
    public static class CondicionAutoFilter extends Filter<CondicionAuto> {

        public CondicionAutoFilter() {}

        public CondicionAutoFilter(CondicionAutoFilter filter) {
            super(filter);
        }

        @Override
        public CondicionAutoFilter copy() {
            return new CondicionAutoFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private EstadoAutoFilter estado;

    private CondicionAutoFilter condicion;

    private LocalDateFilter fechaFabricacion;

    private IntegerFilter km;

    private StringFilter patente;

    private BigDecimalFilter precio;

    private LongFilter marcaId;

    private LongFilter modeloId;

    private LongFilter versionId;

    private LongFilter motorId;

    private Boolean distinct;

    public AutoCriteria() {}

    public AutoCriteria(AutoCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.estado = other.optionalEstado().map(EstadoAutoFilter::copy).orElse(null);
        this.condicion = other.optionalCondicion().map(CondicionAutoFilter::copy).orElse(null);
        this.fechaFabricacion = other.optionalFechaFabricacion().map(LocalDateFilter::copy).orElse(null);
        this.km = other.optionalKm().map(IntegerFilter::copy).orElse(null);
        this.patente = other.optionalPatente().map(StringFilter::copy).orElse(null);
        this.precio = other.optionalPrecio().map(BigDecimalFilter::copy).orElse(null);
        this.marcaId = other.optionalMarcaId().map(LongFilter::copy).orElse(null);
        this.modeloId = other.optionalModeloId().map(LongFilter::copy).orElse(null);
        this.versionId = other.optionalVersionId().map(LongFilter::copy).orElse(null);
        this.motorId = other.optionalMotorId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public AutoCriteria copy() {
        return new AutoCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public EstadoAutoFilter getEstado() {
        return estado;
    }

    public Optional<EstadoAutoFilter> optionalEstado() {
        return Optional.ofNullable(estado);
    }

    public EstadoAutoFilter estado() {
        if (estado == null) {
            setEstado(new EstadoAutoFilter());
        }
        return estado;
    }

    public void setEstado(EstadoAutoFilter estado) {
        this.estado = estado;
    }

    public CondicionAutoFilter getCondicion() {
        return condicion;
    }

    public Optional<CondicionAutoFilter> optionalCondicion() {
        return Optional.ofNullable(condicion);
    }

    public CondicionAutoFilter condicion() {
        if (condicion == null) {
            setCondicion(new CondicionAutoFilter());
        }
        return condicion;
    }

    public void setCondicion(CondicionAutoFilter condicion) {
        this.condicion = condicion;
    }

    public LocalDateFilter getFechaFabricacion() {
        return fechaFabricacion;
    }

    public Optional<LocalDateFilter> optionalFechaFabricacion() {
        return Optional.ofNullable(fechaFabricacion);
    }

    public LocalDateFilter fechaFabricacion() {
        if (fechaFabricacion == null) {
            setFechaFabricacion(new LocalDateFilter());
        }
        return fechaFabricacion;
    }

    public void setFechaFabricacion(LocalDateFilter fechaFabricacion) {
        this.fechaFabricacion = fechaFabricacion;
    }

    public IntegerFilter getKm() {
        return km;
    }

    public Optional<IntegerFilter> optionalKm() {
        return Optional.ofNullable(km);
    }

    public IntegerFilter km() {
        if (km == null) {
            setKm(new IntegerFilter());
        }
        return km;
    }

    public void setKm(IntegerFilter km) {
        this.km = km;
    }

    public StringFilter getPatente() {
        return patente;
    }

    public Optional<StringFilter> optionalPatente() {
        return Optional.ofNullable(patente);
    }

    public StringFilter patente() {
        if (patente == null) {
            setPatente(new StringFilter());
        }
        return patente;
    }

    public void setPatente(StringFilter patente) {
        this.patente = patente;
    }

    public BigDecimalFilter getPrecio() {
        return precio;
    }

    public Optional<BigDecimalFilter> optionalPrecio() {
        return Optional.ofNullable(precio);
    }

    public BigDecimalFilter precio() {
        if (precio == null) {
            setPrecio(new BigDecimalFilter());
        }
        return precio;
    }

    public void setPrecio(BigDecimalFilter precio) {
        this.precio = precio;
    }

    public LongFilter getMarcaId() {
        return marcaId;
    }

    public Optional<LongFilter> optionalMarcaId() {
        return Optional.ofNullable(marcaId);
    }

    public LongFilter marcaId() {
        if (marcaId == null) {
            setMarcaId(new LongFilter());
        }
        return marcaId;
    }

    public void setMarcaId(LongFilter marcaId) {
        this.marcaId = marcaId;
    }

    public LongFilter getModeloId() {
        return modeloId;
    }

    public Optional<LongFilter> optionalModeloId() {
        return Optional.ofNullable(modeloId);
    }

    public LongFilter modeloId() {
        if (modeloId == null) {
            setModeloId(new LongFilter());
        }
        return modeloId;
    }

    public void setModeloId(LongFilter modeloId) {
        this.modeloId = modeloId;
    }

    public LongFilter getVersionId() {
        return versionId;
    }

    public Optional<LongFilter> optionalVersionId() {
        return Optional.ofNullable(versionId);
    }

    public LongFilter versionId() {
        if (versionId == null) {
            setVersionId(new LongFilter());
        }
        return versionId;
    }

    public void setVersionId(LongFilter versionId) {
        this.versionId = versionId;
    }

    public LongFilter getMotorId() {
        return motorId;
    }

    public Optional<LongFilter> optionalMotorId() {
        return Optional.ofNullable(motorId);
    }

    public LongFilter motorId() {
        if (motorId == null) {
            setMotorId(new LongFilter());
        }
        return motorId;
    }

    public void setMotorId(LongFilter motorId) {
        this.motorId = motorId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AutoCriteria that = (AutoCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(estado, that.estado) &&
            Objects.equals(condicion, that.condicion) &&
            Objects.equals(fechaFabricacion, that.fechaFabricacion) &&
            Objects.equals(km, that.km) &&
            Objects.equals(patente, that.patente) &&
            Objects.equals(precio, that.precio) &&
            Objects.equals(marcaId, that.marcaId) &&
            Objects.equals(modeloId, that.modeloId) &&
            Objects.equals(versionId, that.versionId) &&
            Objects.equals(motorId, that.motorId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, estado, condicion, fechaFabricacion, km, patente, precio, marcaId, modeloId, versionId, motorId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AutoCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalEstado().map(f -> "estado=" + f + ", ").orElse("") +
            optionalCondicion().map(f -> "condicion=" + f + ", ").orElse("") +
            optionalFechaFabricacion().map(f -> "fechaFabricacion=" + f + ", ").orElse("") +
            optionalKm().map(f -> "km=" + f + ", ").orElse("") +
            optionalPatente().map(f -> "patente=" + f + ", ").orElse("") +
            optionalPrecio().map(f -> "precio=" + f + ", ").orElse("") +
            optionalMarcaId().map(f -> "marcaId=" + f + ", ").orElse("") +
            optionalModeloId().map(f -> "modeloId=" + f + ", ").orElse("") +
            optionalVersionId().map(f -> "versionId=" + f + ", ").orElse("") +
            optionalMotorId().map(f -> "motorId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
