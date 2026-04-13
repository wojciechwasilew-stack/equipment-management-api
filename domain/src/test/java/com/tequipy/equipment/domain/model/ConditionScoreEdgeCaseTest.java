package com.tequipy.equipment.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionScoreEdgeCaseTest {

    @ParameterizedTest
    @CsvSource({
            "0.8, 0.5, true",
            "0.5, 0.5, true",
            "0.5, 0.8, false",
            "1.0, 0.0, true",
            "0.0, 0.0, true",
            "0.0, 1.0, false"
    })
    void shouldEvaluateIsGreaterThanOrEqualCorrectly(double first, double second, boolean expectedResult) {
        // given
        var scoreA = ConditionScore.of(first);
        var scoreB = ConditionScore.of(second);

        // when
        var result = scoreA.isGreaterThanOrEqual(scoreB);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldCreateFromBigDecimalFactory() {
        // when
        var score = ConditionScore.of(BigDecimal.valueOf(0.75));

        // then
        assertThat(score.value()).isEqualByComparingTo(BigDecimal.valueOf(0.75));
    }

    @Test
    void shouldHandleBoundaryValueZero() {
        // when
        var score = ConditionScore.of(BigDecimal.ZERO);

        // then
        assertThat(score.value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldHandleBoundaryValueOne() {
        // when
        var score = ConditionScore.of(BigDecimal.ONE);

        // then
        assertThat(score.value()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void shouldSupportRecordEquality() {
        // given
        var scoreA = ConditionScore.of(0.5);
        var scoreB = ConditionScore.of(0.5);

        // then
        assertThat(scoreA).isEqualTo(scoreB);
        assertThat(scoreA.hashCode()).isEqualTo(scoreB.hashCode());
    }
}
