package com.tequipy.equipment.adapter.in.rest.dto;

import java.util.List;
import java.util.UUID;

public record AllocationResponse(
        UUID id,
        String employeeId,
        String state,
        List<PolicyItemResponse> policyItems,
        List<UUID> allocatedEquipmentIds
) {

    public record PolicyItemResponse(
            String equipmentType,
            int quantity,
            java.math.BigDecimal minimumConditionScore,
            String preferredBrand,
            boolean preferRecent
    ) {
    }
}
