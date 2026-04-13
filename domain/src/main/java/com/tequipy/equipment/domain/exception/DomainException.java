package com.tequipy.equipment.domain.exception;

public sealed class DomainException extends RuntimeException
        permits EquipmentNotFoundException, AllocationNotFoundException,
                InvalidEquipmentStateTransitionException, InvalidAllocationStateTransitionException,
                AllocationFailedException {

    public DomainException(String message) {
        super(message);
    }
}
