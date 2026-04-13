package com.tequipy.equipment.application.port.in;

import java.util.UUID;

public interface RetireEquipmentUseCase {

    void retire(UUID equipmentId, String reason);
}
