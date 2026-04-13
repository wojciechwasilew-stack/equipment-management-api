package com.tequipy.equipment.domain.exception;

import com.tequipy.equipment.domain.model.AllocationState;

import java.util.UUID;

public final class InvalidAllocationStateTransitionException extends DomainException {

    public InvalidAllocationStateTransitionException(UUID allocationId, AllocationState currentState,
                                                     AllocationState targetState) {
        super("Cannot transition allocation " + allocationId + " from " + currentState + " to " + targetState);
    }
}
