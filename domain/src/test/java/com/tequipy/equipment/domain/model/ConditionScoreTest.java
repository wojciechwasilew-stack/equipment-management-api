package com.tequipy.equipment.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConditionScoreTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.5, 1.0, 0.01, 0.99})
    void shouldCreateValidConditionScoreWhenValueInRange(double value) {
        // when
        var score = ConditionScore.of(value);

        // then
        assertThat(score.value()).isEqualByComparingTo(BigDecimal.valueOf(value));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -1.0, -100.0})
    void shouldThrowWhenValueBelowZero(double value) {
        assertThatThrownBy(() -> ConditionScore.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0.0 and 1.0");
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.01, 2.0, 100.0})
    void shouldThrowWhenValueAboveOne(double value) {
        assertThatThrownBy(() -> ConditionScore.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0.0 and 1.0");
    }

    @Test
    void shouldThrowWhenValueNull() {
        assertThatThrownBy(() -> ConditionScore.of((BigDecimal) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Condition score value is required");
    }

    @ParameterizedTest
    @CsvSource({
            "0.8, 0.5, 1",
            "0.5, 0.8, -1",
            "0.5, 0.5, 0"
    })
    void shouldCompareCorrectly(double first, double second, int expectedSign) {
        // given
        var scoreA = ConditionScore.of(first);
        var scoreB = ConditionScore.of(second);

        // when
        var result = scoreA.compareTo(scoreB);

        // then
        assertThat(Integer.signum(result)).isEqualTo(expectedSign);
    }
}
