package com.concesionaria.app.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.concesionaria.app.domain.Motor} entity. This class is used
 * in {@link com.concesionaria.app.web.rest.MotorResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /motors?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MotorCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter nombre;

    private IntegerFilter cilindradaCc;

    private IntegerFilter cilindroCant;

    private IntegerFilter potenciaHp;

    private BooleanFilter turbo;

    private LongFilter versionesId;

    private Boolean distinct;

    public MotorCriteria() {}

    public MotorCriteria(MotorCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.nombre = other.optionalNombre().map(StringFilter::copy).orElse(null);
        this.cilindradaCc = other.optionalCilindradaCc().map(IntegerFilter::copy).orElse(null);
        this.cilindroCant = other.optionalCilindroCant().map(IntegerFilter::copy).orElse(null);
        this.potenciaHp = other.optionalPotenciaHp().map(IntegerFilter::copy).orElse(null);
        this.turbo = other.optionalTurbo().map(BooleanFilter::copy).orElse(null);
        this.versionesId = other.optionalVersionesId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public MotorCriteria copy() {
        return new MotorCriteria(this);
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

    public IntegerFilter getCilindradaCc() {
        return cilindradaCc;
    }

    public Optional<IntegerFilter> optionalCilindradaCc() {
        return Optional.ofNullable(cilindradaCc);
    }

    public IntegerFilter cilindradaCc() {
        if (cilindradaCc == null) {
            setCilindradaCc(new IntegerFilter());
        }
        return cilindradaCc;
    }

    public void setCilindradaCc(IntegerFilter cilindradaCc) {
        this.cilindradaCc = cilindradaCc;
    }

    public IntegerFilter getCilindroCant() {
        return cilindroCant;
    }

    public Optional<IntegerFilter> optionalCilindroCant() {
        return Optional.ofNullable(cilindroCant);
    }

    public IntegerFilter cilindroCant() {
        if (cilindroCant == null) {
            setCilindroCant(new IntegerFilter());
        }
        return cilindroCant;
    }

    public void setCilindroCant(IntegerFilter cilindroCant) {
        this.cilindroCant = cilindroCant;
    }

    public IntegerFilter getPotenciaHp() {
        return potenciaHp;
    }

    public Optional<IntegerFilter> optionalPotenciaHp() {
        return Optional.ofNullable(potenciaHp);
    }

    public IntegerFilter potenciaHp() {
        if (potenciaHp == null) {
            setPotenciaHp(new IntegerFilter());
        }
        return potenciaHp;
    }

    public void setPotenciaHp(IntegerFilter potenciaHp) {
        this.potenciaHp = potenciaHp;
    }

    public BooleanFilter getTurbo() {
        return turbo;
    }

    public Optional<BooleanFilter> optionalTurbo() {
        return Optional.ofNullable(turbo);
    }

    public BooleanFilter turbo() {
        if (turbo == null) {
            setTurbo(new BooleanFilter());
        }
        return turbo;
    }

    public void setTurbo(BooleanFilter turbo) {
        this.turbo = turbo;
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
        final MotorCriteria that = (MotorCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nombre, that.nombre) &&
            Objects.equals(cilindradaCc, that.cilindradaCc) &&
            Objects.equals(cilindroCant, that.cilindroCant) &&
            Objects.equals(potenciaHp, that.potenciaHp) &&
            Objects.equals(turbo, that.turbo) &&
            Objects.equals(versionesId, that.versionesId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, cilindradaCc, cilindroCant, potenciaHp, turbo, versionesId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MotorCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNombre().map(f -> "nombre=" + f + ", ").orElse("") +
            optionalCilindradaCc().map(f -> "cilindradaCc=" + f + ", ").orElse("") +
            optionalCilindroCant().map(f -> "cilindroCant=" + f + ", ").orElse("") +
            optionalPotenciaHp().map(f -> "potenciaHp=" + f + ", ").orElse("") +
            optionalTurbo().map(f -> "turbo=" + f + ", ").orElse("") +
            optionalVersionesId().map(f -> "versionesId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
