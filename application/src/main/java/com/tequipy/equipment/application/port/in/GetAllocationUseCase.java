package com.tequipy.equipment.application.port.in;

import com.tequipy.equipment.domain.model.AllocationRequest;

import java.util.UUID;

public interface GetAllocationUseCase {

    AllocationRequest getById(UUID id);
}
