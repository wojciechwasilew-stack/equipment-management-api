package com.tequipy.equipment.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record ConditionScore(BigDecimal value) implements Comparable<ConditionScore> {

    public ConditionScore {
        Objects.requireNonNull(value, "Condition score value is required");
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "Condition score must be between 0.0 and 1.0, got: " + value);
        }
    }

    public static ConditionScore of(BigDecimal value) {
        return new ConditionScore(value);
    }

    public static ConditionScore of(double value) {
        return new ConditionScore(BigDecimal.valueOf(value));
    }

    public boolean isGreaterThanOrEqual(ConditionScore other) {
        return value.compareTo(other.value) >= 0;
    }

    @Override
    public int compareTo(ConditionScore other) {
        return this.value.compareTo(other.value);
    }
}
