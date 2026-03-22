package com.concesionaria.app.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class VersionCriteriaTest {

    @Test
    void newVersionCriteriaHasAllFiltersNullTest() {
        var versionCriteria = new VersionCriteria();
        assertThat(versionCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void versionCriteriaFluentMethodsCreatesFiltersTest() {
        var versionCriteria = new VersionCriteria();

        setAllFilters(versionCriteria);

        assertThat(versionCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void versionCriteriaCopyCreatesNullFilterTest() {
        var versionCriteria = new VersionCriteria();
        var copy = versionCriteria.copy();

        assertThat(versionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(versionCriteria)
        );
    }

    @Test
    void versionCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var versionCriteria = new VersionCriteria();
        setAllFilters(versionCriteria);

        var copy = versionCriteria.copy();

        assertThat(versionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(versionCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var versionCriteria = new VersionCriteria();

        assertThat(versionCriteria).hasToString("VersionCriteria{}");
    }

    private static void setAllFilters(VersionCriteria versionCriteria) {
        versionCriteria.id();
        versionCriteria.nombre();
        versionCriteria.descripcion();
        versionCriteria.anioInicio();
        versionCriteria.anioFin();
        versionCriteria.motoresId();
        versionCriteria.modelosId();
        versionCriteria.distinct();
    }

    private static Condition<VersionCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNombre()) &&
                condition.apply(criteria.getDescripcion()) &&
                condition.apply(criteria.getAnioInicio()) &&
                condition.apply(criteria.getAnioFin()) &&
                condition.apply(criteria.getMotoresId()) &&
                condition.apply(criteria.getModelosId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<VersionCriteria> copyFiltersAre(VersionCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNombre(), copy.getNombre()) &&
                condition.apply(criteria.getDescripcion(), copy.getDescripcion()) &&
                condition.apply(criteria.getAnioInicio(), copy.getAnioInicio()) &&
                condition.apply(criteria.getAnioFin(), copy.getAnioFin()) &&
                condition.apply(criteria.getMotoresId(), copy.getMotoresId()) &&
                condition.apply(criteria.getModelosId(), copy.getModelosId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
