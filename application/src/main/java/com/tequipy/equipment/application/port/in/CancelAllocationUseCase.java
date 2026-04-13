package com.tequipy.equipment.application.port.in;

import java.util.UUID;

public interface CancelAllocationUseCase {

    void cancel(UUID id);
}
