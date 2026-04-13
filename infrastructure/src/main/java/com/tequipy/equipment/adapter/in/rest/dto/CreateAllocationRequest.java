package com.tequipy.equipment.adapter.in.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateAllocationRequest(
        @NotBlank String employeeId,
        @NotEmpty @Valid List<PolicyItemRequest> policyItems
) {
}
