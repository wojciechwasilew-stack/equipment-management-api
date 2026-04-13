package com.tequipy.equipment.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EquipmentResponse(
        UUID id,
        String type,
        String brand,
        String model,
        String state,
        BigDecimal conditionScore,
        LocalDate purchaseDate,
        String retireReason
) {
}
