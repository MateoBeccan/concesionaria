package com.concesionaria.app.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class Prueba1CriteriaTest {

    @Test
    void newPrueba1CriteriaHasAllFiltersNullTest() {
        var prueba1Criteria = new Prueba1Criteria();
        assertThat(prueba1Criteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void prueba1CriteriaFluentMethodsCreatesFiltersTest() {
        var prueba1Criteria = new Prueba1Criteria();

        setAllFilters(prueba1Criteria);

        assertThat(prueba1Criteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void prueba1CriteriaCopyCreatesNullFilterTest() {
        var prueba1Criteria = new Prueba1Criteria();
        var copy = prueba1Criteria.copy();

        assertThat(prueba1Criteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(prueba1Criteria)
        );
    }

    @Test
    void prueba1CriteriaCopyDuplicatesEveryExistingFilterTest() {
        var prueba1Criteria = new Prueba1Criteria();
        setAllFilters(prueba1Criteria);

        var copy = prueba1Criteria.copy();

        assertThat(prueba1Criteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(prueba1Criteria)
        );
    }

    @Test
    void toStringVerifier() {
        var prueba1Criteria = new Prueba1Criteria();

        assertThat(prueba1Criteria).hasToString("Prueba1Criteria{}");
    }

    private static void setAllFilters(Prueba1Criteria prueba1Criteria) {
        prueba1Criteria.id();
        prueba1Criteria.distinct();
    }

    private static Condition<Prueba1Criteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria -> condition.apply(criteria.getId()) && condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<Prueba1Criteria> copyFiltersAre(Prueba1Criteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria -> condition.apply(criteria.getId(), copy.getId()) && condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
