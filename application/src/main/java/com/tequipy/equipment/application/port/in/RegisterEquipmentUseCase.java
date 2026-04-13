package com.tequipy.equipment.application.port.in;

import java.util.UUID;

public interface RegisterEquipmentUseCase {

    UUID register(RegisterEquipmentCommand command);
}
