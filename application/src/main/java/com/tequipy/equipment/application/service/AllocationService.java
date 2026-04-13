package com.tequipy.equipment.application.service;

import com.tequipy.equipment.application.port.in.CancelAllocationUseCase;
import com.tequipy.equipment.application.port.in.ConfirmAllocationUseCase;
import com.tequipy.equipment.application.port.in.CreateAllocationCommand;
import com.tequipy.equipment.application.port.in.CreateAllocationUseCase;
import com.tequipy.equipment.application.port.in.GetAllocationUseCase;
import com.tequipy.equipment.application.port.out.LoadAllocationPort;
import com.tequipy.equipment.application.port.out.LoadEquipmentPort;
import com.tequipy.equipment.application.port.out.PublishAllocationEventPort;
import com.tequipy.equipment.application.port.out.SaveAllocationPort;
import com.tequipy.equipment.application.port.out.SaveEquipmentPort;
import com.tequipy.equipment.domain.event.AllocationCreatedEvent;
import com.tequipy.equipment.domain.exception.AllocationNotFoundException;
import com.tequipy.equipment.domain.exception.EquipmentNotFoundException;
import com.tequipy.equipment.domain.model.AllocationRequest;
import com.tequipy.equipment.domain.model.AllocationState;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.PolicyItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AllocationService implements CreateAllocationUseCase, GetAllocationUseCase,
        ConfirmAllocationUseCase, CancelAllocationUseCase {

    private final SaveAllocationPort saveAllocationPort;
    private final LoadAllocationPort loadAllocationPort;
    private final SaveEquipmentPort saveEquipmentPort;
    private final LoadEquipmentPort loadEquipmentPort;
    private final PublishAllocationEventPort publishAllocationEventPort;

    @Override
    public UUID create(CreateAllocationCommand command) {
        var allocationId = UUID.randomUUID();
        var policyItems = command.policyItems().stream()
                .map(this::toPolicyItem)
                .toList();

        var allocationRequest = new AllocationRequest(
                allocationId,
                command.employeeId(),
                policyItems,
                AllocationState.PENDING,
                List.of()
        );

        saveAllocationPort.save(allocationRequest);
        publishAllocationEventPort.publish(new AllocationCreatedEvent(allocationId));
        log.info("Created allocation request {} for employee {}", allocationId, command.employeeId());
        return allocationId;
    }

    @Override
    public AllocationRequest getById(UUID id) {
        return loadAllocationPort.findById(id)
                .orElseThrow(() -> new AllocationNotFoundException(id));
    }

    @Override
    public void confirm(UUID id) {
        var allocation = loadAllocationPort.findById(id)
                .orElseThrow(() -> new AllocationNotFoundException(id));

        allocation.confirm();

        for (UUID equipmentId : allocation.allocatedEquipmentIds()) {
            var equipment = loadEquipmentPort.findById(equipmentId)
                    .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));
            equipment.assign();
            saveEquipmentPort.save(equipment);
        }

        saveAllocationPort.save(allocation);
        log.info("Confirmed allocation request {}", id);
    }

    @Override
    public void cancel(UUID id) {
        var allocation = loadAllocationPort.findById(id)
                .orElseThrow(() -> new AllocationNotFoundException(id));

        allocation.cancel();

        for (UUID equipmentId : allocation.allocatedEquipmentIds()) {
            var equipment = loadEquipmentPort.findById(equipmentId)
                    .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));
            equipment.release();
            saveEquipmentPort.save(equipment);
        }

        saveAllocationPort.save(allocation);
        log.info("Cancelled allocation request {}", id);
    }

    private PolicyItem toPolicyItem(CreateAllocationCommand.PolicyItemCommand command) {
        return new PolicyItem(
                command.equipmentType(),
                command.quantity(),
                ConditionScore.of(command.minimumConditionScore()),
                command.preferredBrand(),
                command.preferRecent()
        );
    }
}
