package com.tequipy.equipment.application.port.in;

import java.util.UUID;

public interface ConfirmAllocationUseCase {

    void confirm(UUID id);
}
