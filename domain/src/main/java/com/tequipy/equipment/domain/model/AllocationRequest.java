package com.tequipy.equipment.domain.model;

import com.tequipy.equipment.domain.exception.InvalidAllocationStateTransitionException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AllocationRequest {

    private final UUID id;
    private final String employeeId;
    private final List<PolicyItem> policyItems;
    private AllocationState state;
    private List<UUID> allocatedEquipmentIds;

    public AllocationRequest(UUID id, String employeeId, List<PolicyItem> policyItems,
                             AllocationState state, List<UUID> allocatedEquipmentIds) {
        this.id = Objects.requireNonNull(id, "Allocation request id is required");
        this.employeeId = Objects.requireNonNull(employeeId, "Employee id is required");
        this.policyItems = List.copyOf(Objects.requireNonNull(policyItems, "Policy items are required"));
        this.state = Objects.requireNonNull(state, "State is required");
        this.allocatedEquipmentIds = allocatedEquipmentIds != null ? List.copyOf(allocatedEquipmentIds) : List.of();
    }

    public void markAllocated(List<UUID> equipmentIds) {
        if (state != AllocationState.PENDING) {
            throw new InvalidAllocationStateTransitionException(id, state, AllocationState.ALLOCATED);
        }
        this.allocatedEquipmentIds = List.copyOf(equipmentIds);
        this.state = AllocationState.ALLOCATED;
    }

    public void confirm() {
        if (state != AllocationState.ALLOCATED) {
            throw new InvalidAllocationStateTransitionException(id, state, AllocationState.CONFIRMED);
        }
        this.state = AllocationState.CONFIRMED;
    }

    public void cancel() {
        if (state != AllocationState.ALLOCATED && state != AllocationState.PENDING) {
            throw new InvalidAllocationStateTransitionException(id, state, AllocationState.CANCELLED);
        }
        this.state = AllocationState.CANCELLED;
    }

    public void fail() {
        if (state != AllocationState.PENDING) {
            throw new InvalidAllocationStateTransitionException(id, state, AllocationState.FAILED);
        }
        this.state = AllocationState.FAILED;
    }

    public UUID id() { return id; }
    public String employeeId() { return employeeId; }
    public List<PolicyItem> policyItems() { return policyItems; }
    public AllocationState state() { return state; }
    public List<UUID> allocatedEquipmentIds() { return allocatedEquipmentIds; }
}
