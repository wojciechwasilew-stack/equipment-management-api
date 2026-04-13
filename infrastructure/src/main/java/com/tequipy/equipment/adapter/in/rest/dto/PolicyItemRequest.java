package com.tequipy.equipment.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PolicyItemRequest(
        @NotNull String equipmentType,
        @Min(1) int quantity,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal minimumConditionScore,
        String preferredBrand,
        boolean preferRecent
) {
}
