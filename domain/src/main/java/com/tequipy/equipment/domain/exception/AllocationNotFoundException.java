package com.tequipy.equipment.domain.exception;

import java.util.UUID;

public final class AllocationNotFoundException extends DomainException {

    public AllocationNotFoundException(UUID allocationId) {
        super("Allocation request not found with id: " + allocationId);
    }
}
