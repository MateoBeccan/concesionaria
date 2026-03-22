package com.concesionaria.app.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ModeloCriteriaTest {

    @Test
    void newModeloCriteriaHasAllFiltersNullTest() {
        var modeloCriteria = new ModeloCriteria();
        assertThat(modeloCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void modeloCriteriaFluentMethodsCreatesFiltersTest() {
        var modeloCriteria = new ModeloCriteria();

        setAllFilters(modeloCriteria);

        assertThat(modeloCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void modeloCriteriaCopyCreatesNullFilterTest() {
        var modeloCriteria = new ModeloCriteria();
        var copy = modeloCriteria.copy();

        assertThat(modeloCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(modeloCriteria)
        );
    }

    @Test
    void modeloCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var modeloCriteria = new ModeloCriteria();
        setAllFilters(modeloCriteria);

        var copy = modeloCriteria.copy();

        assertThat(modeloCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(modeloCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var modeloCriteria = new ModeloCriteria();

        assertThat(modeloCriteria).hasToString("ModeloCriteria{}");
    }

    private static void setAllFilters(ModeloCriteria modeloCriteria) {
        modeloCriteria.id();
        modeloCriteria.nombre();
        modeloCriteria.anioLanzamiento();
        modeloCriteria.carroceria();
        modeloCriteria.marcaId();
        modeloCriteria.versionesId();
        modeloCriteria.distinct();
    }

    private static Condition<ModeloCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNombre()) &&
                condition.apply(criteria.getAnioLanzamiento()) &&
                condition.apply(criteria.getCarroceria()) &&
                condition.apply(criteria.getMarcaId()) &&
                condition.apply(criteria.getVersionesId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ModeloCriteria> copyFiltersAre(ModeloCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNombre(), copy.getNombre()) &&
                condition.apply(criteria.getAnioLanzamiento(), copy.getAnioLanzamiento()) &&
                condition.apply(criteria.getCarroceria(), copy.getCarroceria()) &&
                condition.apply(criteria.getMarcaId(), copy.getMarcaId()) &&
                condition.apply(criteria.getVersionesId(), copy.getVersionesId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
