package com.concesionaria.app.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.concesionaria.app.domain.Modelo} entity. This class is used
 * in {@link com.concesionaria.app.web.rest.ModeloResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /modelos?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ModeloCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter nombre;

    private IntegerFilter anioLanzamiento;

    private StringFilter carroceria;

    private LongFilter marcaId;

    private LongFilter versionesId;

    private Boolean distinct;

    public ModeloCriteria() {}

    public ModeloCriteria(ModeloCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.nombre = other.optionalNombre().map(StringFilter::copy).orElse(null);
        this.anioLanzamiento = other.optionalAnioLanzamiento().map(IntegerFilter::copy).orElse(null);
        this.carroceria = other.optionalCarroceria().map(StringFilter::copy).orElse(null);
        this.marcaId = other.optionalMarcaId().map(LongFilter::copy).orElse(null);
        this.versionesId = other.optionalVersionesId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ModeloCriteria copy() {
        return new ModeloCriteria(this);
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

    public IntegerFilter getAnioLanzamiento() {
        return anioLanzamiento;
    }

    public Optional<IntegerFilter> optionalAnioLanzamiento() {
        return Optional.ofNullable(anioLanzamiento);
    }

    public IntegerFilter anioLanzamiento() {
        if (anioLanzamiento == null) {
            setAnioLanzamiento(new IntegerFilter());
        }
        return anioLanzamiento;
    }

    public void setAnioLanzamiento(IntegerFilter anioLanzamiento) {
        this.anioLanzamiento = anioLanzamiento;
    }

    public StringFilter getCarroceria() {
        return carroceria;
    }

    public Optional<StringFilter> optionalCarroceria() {
        return Optional.ofNullable(carroceria);
    }

    public StringFilter carroceria() {
        if (carroceria == null) {
            setCarroceria(new StringFilter());
        }
        return carroceria;
    }

    public void setCarroceria(StringFilter carroceria) {
        this.carroceria = carroceria;
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

    public LongFilter getVersionesId() {
        return versionesId;
    }

    public Optional<LongFilter> optionalVersionesId() {
        return Optional.ofNullable(versionesId);
    }

    public LongFilter versionesId() {
        if (versionesId == null) {
            setVersionesId(new LongFilter());
        }
        return versionesId;
    }

    public void setVersionesId(LongFilter versionesId) {
        this.versionesId = versionesId;
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
        final ModeloCriteria that = (ModeloCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nombre, that.nombre) &&
            Objects.equals(anioLanzamiento, that.anioLanzamiento) &&
            Objects.equals(carroceria, that.carroceria) &&
            Objects.equals(marcaId, that.marcaId) &&
            Objects.equals(versionesId, that.versionesId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, anioLanzamiento, carroceria, marcaId, versionesId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ModeloCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNombre().map(f -> "nombre=" + f + ", ").orElse("") +
            optionalAnioLanzamiento().map(f -> "anioLanzamiento=" + f + ", ").orElse("") +
            optionalCarroceria().map(f -> "carroceria=" + f + ", ").orElse("") +
            optionalMarcaId().map(f -> "marcaId=" + f + ", ").orElse("") +
            optionalVersionesId().map(f -> "versionesId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
