package com.tequipy.equipment.domain.model;

import java.util.Objects;

public record PolicyItem(
        EquipmentType equipmentType,
        int quantity,
        ConditionScore minimumConditionScore,
        String preferredBrand,
        boolean preferRecent
) {

    public PolicyItem {
        Objects.requireNonNull(equipmentType, "Equipment type is required");
        Objects.requireNonNull(minimumConditionScore, "Minimum condition score is required");
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1, got: " + quantity);
        }
    }
}
