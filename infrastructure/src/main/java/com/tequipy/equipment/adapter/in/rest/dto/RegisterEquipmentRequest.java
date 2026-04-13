package com.tequipy.equipment.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterEquipmentRequest(
        @NotNull String type,
        @NotBlank String brand,
        @NotBlank String model,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal conditionScore,
        @NotNull LocalDate purchaseDate
) {
}
