package com.tequipy.equipment.domain.exception;

import java.util.UUID;

public final class AllocationFailedException extends DomainException {

    public AllocationFailedException(UUID allocationRequestId) {
        super("Allocation failed for request: " + allocationRequestId);
    }
}
