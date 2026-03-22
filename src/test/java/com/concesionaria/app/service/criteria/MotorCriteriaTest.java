package com.concesionaria.app.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class MotorCriteriaTest {

    @Test
    void newMotorCriteriaHasAllFiltersNullTest() {
        var motorCriteria = new MotorCriteria();
        assertThat(motorCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void motorCriteriaFluentMethodsCreatesFiltersTest() {
        var motorCriteria = new MotorCriteria();

        setAllFilters(motorCriteria);

        assertThat(motorCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void motorCriteriaCopyCreatesNullFilterTest() {
        var motorCriteria = new MotorCriteria();
        var copy = motorCriteria.copy();

        assertThat(motorCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(motorCriteria)
        );
    }

    @Test
    void motorCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var motorCriteria = new MotorCriteria();
        setAllFilters(motorCriteria);

        var copy = motorCriteria.copy();

        assertThat(motorCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(motorCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var motorCriteria = new MotorCriteria();

        assertThat(motorCriteria).hasToString("MotorCriteria{}");
    }

    private static void setAllFilters(MotorCriteria motorCriteria) {
        motorCriteria.id();
        motorCriteria.nombre();
        motorCriteria.cilindradaCc();
        motorCriteria.cilindroCant();
        motorCriteria.potenciaHp();
        motorCriteria.turbo();
        motorCriteria.versionesId();
        motorCriteria.distinct();
    }

    private static Condition<MotorCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNombre()) &&
                condition.apply(criteria.getCilindradaCc()) &&
                condition.apply(criteria.getCilindroCant()) &&
                condition.apply(criteria.getPotenciaHp()) &&
                condition.apply(criteria.getTurbo()) &&
                condition.apply(criteria.getVersionesId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<MotorCriteria> copyFiltersAre(MotorCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNombre(), copy.getNombre()) &&
                condition.apply(criteria.getCilindradaCc(), copy.getCilindradaCc()) &&
                condition.apply(criteria.getCilindroCant(), copy.getCilindroCant()) &&
                condition.apply(criteria.getPotenciaHp(), copy.getPotenciaHp()) &&
                condition.apply(criteria.getTurbo(), copy.getTurbo()) &&
                condition.apply(criteria.getVersionesId(), copy.getVersionesId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
