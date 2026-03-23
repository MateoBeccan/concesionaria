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
        autoCriteria.distinct();
    }

    private static Condition<AutoCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria -> condition.apply(criteria.getId()) && condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AutoCriteria> copyFiltersAre(AutoCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria -> condition.apply(criteria.getId(), copy.getId()) && condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
