package com.tequipy.equipment.application.port.out;

import com.tequipy.equipment.domain.model.AllocationRequest;

public interface SaveAllocationPort {

    AllocationRequest save(AllocationRequest allocationRequest);
}
