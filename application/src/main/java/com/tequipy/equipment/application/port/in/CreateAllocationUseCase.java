package com.tequipy.equipment.application.port.in;

import java.util.UUID;

public interface CreateAllocationUseCase {

    UUID create(CreateAllocationCommand command);
}
