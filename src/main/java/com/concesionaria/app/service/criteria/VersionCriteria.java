package com.concesionaria.app.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.concesionaria.app.domain.Version} entity. This class is used
 * in {@link com.concesionaria.app.web.rest.VersionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /versions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VersionCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter nombre;

    private StringFilter descripcion;

    private IntegerFilter anioInicio;

    private IntegerFilter anioFin;

    private LongFilter motoresId;

    private LongFilter modelosId;

    private Boolean distinct;

    public VersionCriteria() {}

    public VersionCriteria(VersionCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.nombre = other.optionalNombre().map(StringFilter::copy).orElse(null);
        this.descripcion = other.optionalDescripcion().map(StringFilter::copy).orElse(null);
        this.anioInicio = other.optionalAnioInicio().map(IntegerFilter::copy).orElse(null);
        this.anioFin = other.optionalAnioFin().map(IntegerFilter::copy).orElse(null);
        this.motoresId = other.optionalMotoresId().map(LongFilter::copy).orElse(null);
        this.modelosId = other.optionalModelosId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public VersionCriteria copy() {
        return new VersionCriteria(this);
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

    public StringFilter getNombre() {
        return nombre;
    }

    public Optional<StringFilter> optionalNombre() {
        return Optional.ofNullable(nombre);
    }

    public StringFilter nombre() {
        if (nombre == null) {
            setNombre(new StringFilter());
        }
        return nombre;
    }

    public void setNombre(StringFilter nombre) {
        this.nombre = nombre;
    }

    public StringFilter getDescripcion() {
        return descripcion;
    }

    public Optional<StringFilter> optionalDescripcion() {
        return Optional.ofNullable(descripcion);
    }

    public StringFilter descripcion() {
        if (descripcion == null) {
            setDescripcion(new StringFilter());
        }
        return descripcion;
    }

    public void setDescripcion(StringFilter descripcion) {
        this.descripcion = descripcion;
    }

    public IntegerFilter getAnioInicio() {
        return anioInicio;
    }

    public Optional<IntegerFilter> optionalAnioInicio() {
        return Optional.ofNullable(anioInicio);
    }

    public IntegerFilter anioInicio() {
        if (anioInicio == null) {
            setAnioInicio(new IntegerFilter());
        }
        return anioInicio;
    }

    public void setAnioInicio(IntegerFilter anioInicio) {
        this.anioInicio = anioInicio;
    }

    public IntegerFilter getAnioFin() {
        return anioFin;
    }

    public Optional<IntegerFilter> optionalAnioFin() {
        return Optional.ofNullable(anioFin);
    }

    public IntegerFilter anioFin() {
        if (anioFin == null) {
            setAnioFin(new IntegerFilter());
        }
        return anioFin;
    }

    public void setAnioFin(IntegerFilter anioFin) {
        this.anioFin = anioFin;
    }

    public LongFilter getMotoresId() {
        return motoresId;
    }

    public Optional<LongFilter> optionalMotoresId() {
        return Optional.ofNullable(motoresId);
    }

    public LongFilter motoresId() {
        if (motoresId == null) {
            setMotoresId(new LongFilter());
        }
        return motoresId;
    }

    public void setMotoresId(LongFilter motoresId) {
        this.motoresId = motoresId;
    }

    public LongFilter getModelosId() {
        return modelosId;
    }

    public Optional<LongFilter> optionalModelosId() {
        return Optional.ofNullable(modelosId);
    }

    public LongFilter modelosId() {
        if (modelosId == null) {
            setModelosId(new LongFilter());
        }
        return modelosId;
    }

    public void setModelosId(LongFilter modelosId) {
        this.modelosId = modelosId;
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
        final VersionCriteria that = (VersionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nombre, that.nombre) &&
            Objects.equals(descripcion, that.descripcion) &&
            Objects.equals(anioInicio, that.anioInicio) &&
            Objects.equals(anioFin, that.anioFin) &&
            Objects.equals(motoresId, that.motoresId) &&
            Objects.equals(modelosId, that.modelosId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, descripcion, anioInicio, anioFin, motoresId, modelosId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VersionCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNombre().map(f -> "nombre=" + f + ", ").orElse("") +
            optionalDescripcion().map(f -> "descripcion=" + f + ", ").orElse("") +
            optionalAnioInicio().map(f -> "anioInicio=" + f + ", ").orElse("") +
            optionalAnioFin().map(f -> "anioFin=" + f + ", ").orElse("") +
            optionalMotoresId().map(f -> "motoresId=" + f + ", ").orElse("") +
            optionalModelosId().map(f -> "modelosId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
