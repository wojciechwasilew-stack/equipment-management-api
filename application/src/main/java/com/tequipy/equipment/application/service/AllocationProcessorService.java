package com.tequipy.equipment.application.service;

import com.tequipy.equipment.application.port.in.ProcessAllocationUseCase;
import com.tequipy.equipment.application.port.out.LoadAllocationPort;
import com.tequipy.equipment.application.port.out.LoadEquipmentPort;
import com.tequipy.equipment.application.port.out.SaveAllocationPort;
import com.tequipy.equipment.application.port.out.SaveEquipmentPort;
import com.tequipy.equipment.domain.exception.AllocationNotFoundException;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.service.AllocationAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AllocationProcessorService implements ProcessAllocationUseCase {

    private final LoadAllocationPort loadAllocationPort;
    private final SaveAllocationPort saveAllocationPort;
    private final LoadEquipmentPort loadEquipmentPort;
    private final SaveEquipmentPort saveEquipmentPort;

    @Override
    public void process(UUID allocationRequestId) {
        var allocation = loadAllocationPort.findById(allocationRequestId)
                .orElseThrow(() -> new AllocationNotFoundException(allocationRequestId));

        var availableEquipment = loadEquipmentPort.findAllAvailable();
        var allocatedEquipment = AllocationAlgorithm.allocate(allocation.policyItems(), availableEquipment);

        if (allocatedEquipment.isEmpty()) {
            allocation.fail();
            saveAllocationPort.save(allocation);
            log.warn("Allocation failed for request {}: no suitable equipment found", allocationRequestId);
            return;
        }

        var equipmentIds = allocatedEquipment.stream()
                .map(Equipment::id)
                .toList();

        for (Equipment equipment : allocatedEquipment) {
            equipment.reserve();
            saveEquipmentPort.save(equipment);
        }

        allocation.markAllocated(equipmentIds);
        saveAllocationPort.save(allocation);
        log.info("Processed allocation request {} with {} equipment items", allocationRequestId, equipmentIds.size());
    }
}
