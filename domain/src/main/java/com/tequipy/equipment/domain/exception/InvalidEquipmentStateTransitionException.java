package com.tequipy.equipment.domain.exception;

import com.tequipy.equipment.domain.model.EquipmentState;

import java.util.UUID;

public final class InvalidEquipmentStateTransitionException extends DomainException {

    public InvalidEquipmentStateTransitionException(UUID equipmentId, EquipmentState currentState,
                                                    EquipmentState targetState) {
        super("Cannot transition equipment " + equipmentId + " from " + currentState + " to " + targetState);
    }
}
