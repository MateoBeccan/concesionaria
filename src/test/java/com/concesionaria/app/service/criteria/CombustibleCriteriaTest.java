package com.concesionaria.app.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CombustibleCriteriaTest {

    @Test
    void newCombustibleCriteriaHasAllFiltersNullTest() {
        var combustibleCriteria = new CombustibleCriteria();
        assertThat(combustibleCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void combustibleCriteriaFluentMethodsCreatesFiltersTest() {
        var combustibleCriteria = new CombustibleCriteria();

        setAllFilters(combustibleCriteria);

        assertThat(combustibleCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void combustibleCriteriaCopyCreatesNullFilterTest() {
        var combustibleCriteria = new CombustibleCriteria();
        var copy = combustibleCriteria.copy();

        assertThat(combustibleCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(combustibleCriteria)
        );
    }

    @Test
    void combustibleCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var combustibleCriteria = new CombustibleCriteria();
        setAllFilters(combustibleCriteria);

        var copy = combustibleCriteria.copy();

        assertThat(combustibleCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(combustibleCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var combustibleCriteria = new CombustibleCriteria();

        assertThat(combustibleCriteria).hasToString("CombustibleCriteria{}");
    }

    private static void setAllFilters(CombustibleCriteria combustibleCriteria) {
        combustibleCriteria.id();
        combustibleCriteria.distinct();
    }

    private static Condition<CombustibleCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria -> condition.apply(criteria.getId()) && condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CombustibleCriteria> copyFiltersAre(CombustibleCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria -> condition.apply(criteria.getId(), copy.getId()) && condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
