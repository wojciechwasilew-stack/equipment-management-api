package com.tequipy.equipment.domain.exception;

import java.util.UUID;

public final class EquipmentNotFoundException extends DomainException {

    public EquipmentNotFoundException(UUID equipmentId) {
        super("Equipment not found with id: " + equipmentId);
    }
}
