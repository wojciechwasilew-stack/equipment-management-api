package com.tequipy.equipment.application.port.in;

import com.tequipy.equipment.domain.model.EquipmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record RegisterEquipmentCommand(
        EquipmentType type,
        String brand,
        String model,
        BigDecimal conditionScore,
        LocalDate purchaseDate
) {

    public RegisterEquipmentCommand {
        Objects.requireNonNull(type, "Equipment type is required");
        Objects.requireNonNull(brand, "Brand is required");
        Objects.requireNonNull(model, "Model is required");
        Objects.requireNonNull(conditionScore, "Condition score is required");
        Objects.requireNonNull(purchaseDate, "Purchase date is required");
    }
}
