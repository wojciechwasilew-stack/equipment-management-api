package com.tequipy.equipment.adapter.in.rest;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        ErrorCode errorCode,
        String message,
        String correlationId
) {
}
