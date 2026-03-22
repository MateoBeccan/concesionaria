package com.concesionaria.app.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AutoCriteriaTest {

    @Test
    void newAutoCriteriaHasAllFiltersNullTest() {
        var autoCriteria = new AutoCriteria();
        assertThat(autoCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void autoCriteriaFluentMethodsCreatesFiltersTest() {
        var autoCriteria = new AutoCriteria();

        setAllFilters(autoCriteria);

        assertThat(autoCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void autoCriteriaCopyCreatesNullFilterTest() {
        var autoCriteria = new AutoCriteria();
        var copy = autoCriteria.copy();

        assertThat(autoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(autoCriteria)
        );
    }

    @Test
    void autoCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var autoCriteria = new AutoCriteria();
        setAllFilters(autoCriteria);

        var copy = autoCriteria.copy();

        assertThat(autoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(autoCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var autoCriteria = new AutoCriteria();

        assertThat(autoCriteria).hasToString("AutoCriteria{}");
    }

    private static void setAllFilters(AutoCriteria autoCriteria) {
        autoCriteria.id();
        autoCriteria.estado();
        autoCriteria.condicion();
        autoCriteria.fechaFabricacion();
        autoCriteria.km();
        autoCriteria.patente();
        autoCriteria.precio();
        autoCriteria.marcaId();
        autoCriteria.modeloId();
        autoCriteria.versionId();
        autoCriteria.motorId();
        autoCriteria.distinct();
    }

    private static Condition<AutoCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getEstado()) &&
                condition.apply(criteria.getCondicion()) &&
                condition.apply(criteria.getFechaFabricacion()) &&
                condition.apply(criteria.getKm()) &&
                condition.apply(criteria.getPatente()) &&
                condition.apply(criteria.getPrecio()) &&
                condition.apply(criteria.getMarcaId()) &&
                condition.apply(criteria.getModeloId()) &&
                condition.apply(criteria.getVersionId()) &&
                condition.apply(criteria.getMotorId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AutoCriteria> copyFiltersAre(AutoCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getEstado(), copy.getEstado()) &&
                condition.apply(criteria.getCondicion(), copy.getCondicion()) &&
                condition.apply(criteria.getFechaFabricacion(), copy.getFechaFabricacion()) &&
                condition.apply(criteria.getKm(), copy.getKm()) &&
                condition.apply(criteria.getPatente(), copy.getPatente()) &&
                condition.apply(criteria.getPrecio(), copy.getPrecio()) &&
                condition.apply(criteria.getMarcaId(), copy.getMarcaId()) &&
                condition.apply(criteria.getModeloId(), copy.getModeloId()) &&
                condition.apply(criteria.getVersionId(), copy.getVersionId()) &&
                condition.apply(criteria.getMotorId(), copy.getMotorId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
