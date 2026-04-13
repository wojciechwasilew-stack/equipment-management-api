package com.tequipy.equipment.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record RetireEquipmentRequest(
        @NotBlank String reason
) {
}
