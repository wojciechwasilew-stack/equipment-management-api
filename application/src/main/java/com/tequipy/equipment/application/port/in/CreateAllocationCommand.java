package com.tequipy.equipment.application.port.in;

import com.tequipy.equipment.domain.model.EquipmentType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record CreateAllocationCommand(
        String employeeId,
        List<PolicyItemCommand> policyItems
) {

    public CreateAllocationCommand {
        Objects.requireNonNull(employeeId, "Employee id is required");
        Objects.requireNonNull(policyItems, "Policy items are required");
        policyItems = List.copyOf(policyItems);
    }

    public record PolicyItemCommand(
            EquipmentType equipmentType,
            int quantity,
            BigDecimal minimumConditionScore,
            String preferredBrand,
            boolean preferRecent
    ) {

        public PolicyItemCommand {
            Objects.requireNonNull(equipmentType, "Equipment type is required");
            Objects.requireNonNull(minimumConditionScore, "Minimum condition score is required");
            if (quantity < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1");
            }
        }
    }
}
