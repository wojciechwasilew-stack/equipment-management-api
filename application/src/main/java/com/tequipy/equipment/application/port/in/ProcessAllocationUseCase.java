package com.tequipy.equipment.application.port.in;

import java.util.UUID;

public interface ProcessAllocationUseCase {

    void process(UUID allocationRequestId);
}
