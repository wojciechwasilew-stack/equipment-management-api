package com.tequipy.equipment.application.port.out;

import com.tequipy.equipment.domain.model.AllocationRequest;

import java.util.Optional;
import java.util.UUID;

public interface LoadAllocationPort {

    Optional<AllocationRequest> findById(UUID id);
}
