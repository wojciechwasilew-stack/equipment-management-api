package com.tequipy.equipment.domain.event;

import java.util.Objects;
import java.util.UUID;

public record AllocationCreatedEvent(UUID allocationRequestId) {

    public AllocationCreatedEvent {
        Objects.requireNonNull(allocationRequestId, "Allocation request id is required");
    }
}
