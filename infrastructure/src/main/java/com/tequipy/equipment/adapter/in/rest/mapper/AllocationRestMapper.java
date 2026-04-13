package com.tequipy.equipment.adapter.in.rest.mapper;

import com.tequipy.equipment.adapter.in.rest.dto.AllocationResponse;
import com.tequipy.equipment.adapter.in.rest.dto.CreateAllocationRequest;
import com.tequipy.equipment.adapter.in.rest.dto.PolicyItemRequest;
import com.tequipy.equipment.application.port.in.CreateAllocationCommand;
import com.tequipy.equipment.domain.model.AllocationRequest;
import com.tequipy.equipment.domain.model.PolicyItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AllocationRestMapper {

    default CreateAllocationCommand toCommand(CreateAllocationRequest request) {
        var policyItemCommands = request.policyItems().stream()
                .map(this::toPolicyItemCommand)
                .toList();
        return new CreateAllocationCommand(request.employeeId(), policyItemCommands);
    }

    default CreateAllocationCommand.PolicyItemCommand toPolicyItemCommand(PolicyItemRequest request) {
        return new CreateAllocationCommand.PolicyItemCommand(
                com.tequipy.equipment.domain.model.EquipmentType.valueOf(request.equipmentType()),
                request.quantity(),
                request.minimumConditionScore(),
                request.preferredBrand(),
                request.preferRecent()
        );
    }

    default AllocationResponse toResponse(AllocationRequest allocationRequest) {
        return new AllocationResponse(
                allocationRequest.id(),
                allocationRequest.employeeId(),
                allocationRequest.state().name(),
                toPolicyItemResponses(allocationRequest.policyItems()),
                allocationRequest.allocatedEquipmentIds()
        );
    }

    default List<AllocationResponse.PolicyItemResponse> toPolicyItemResponses(List<PolicyItem> policyItems) {
        return policyItems.stream()
                .map(this::toPolicyItemResponse)
                .toList();
    }

    default AllocationResponse.PolicyItemResponse toPolicyItemResponse(PolicyItem policyItem) {
        return new AllocationResponse.PolicyItemResponse(
                policyItem.equipmentType().name(),
                policyItem.quantity(),
                policyItem.minimumConditionScore().value(),
                policyItem.preferredBrand(),
                policyItem.preferRecent()
        );
    }
}
