package com.tequipy.equipment.adapter.out.persistence.mapper;

import com.tequipy.equipment.adapter.out.persistence.entity.AllocationRequestJpaEntity;
import com.tequipy.equipment.adapter.out.persistence.entity.PolicyItemEmbeddable;
import com.tequipy.equipment.domain.model.AllocationRequest;
import com.tequipy.equipment.domain.model.AllocationState;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.EquipmentType;
import com.tequipy.equipment.domain.model.PolicyItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AllocationPersistenceMapper {

    default AllocationRequestJpaEntity toJpaEntity(AllocationRequest allocationRequest) {
        var entity = new AllocationRequestJpaEntity();
        entity.setId(allocationRequest.id());
        entity.setEmployeeId(allocationRequest.employeeId());
        entity.setState(allocationRequest.state().name());
        entity.setAllocatedEquipmentIds(List.copyOf(allocationRequest.allocatedEquipmentIds()));
        entity.setPolicyItems(toPolicyItemEmbeddables(allocationRequest.policyItems()));
        return entity;
    }

    default AllocationRequest toDomain(AllocationRequestJpaEntity entity) {
        return new AllocationRequest(
                entity.getId(),
                entity.getEmployeeId(),
                toPolicyItems(entity.getPolicyItems()),
                AllocationState.valueOf(entity.getState()),
                List.copyOf(entity.getAllocatedEquipmentIds())
        );
    }

    default List<PolicyItemEmbeddable> toPolicyItemEmbeddables(List<PolicyItem> policyItems) {
        return policyItems.stream()
                .map(this::toPolicyItemEmbeddable)
                .toList();
    }

    default PolicyItemEmbeddable toPolicyItemEmbeddable(PolicyItem policyItem) {
        return new PolicyItemEmbeddable(
                policyItem.equipmentType().name(),
                policyItem.quantity(),
                policyItem.minimumConditionScore().value(),
                policyItem.preferredBrand(),
                policyItem.preferRecent()
        );
    }

    default List<PolicyItem> toPolicyItems(List<PolicyItemEmbeddable> embeddables) {
        return embeddables.stream()
                .map(this::toPolicyItem)
                .toList();
    }

    default PolicyItem toPolicyItem(PolicyItemEmbeddable embeddable) {
        return new PolicyItem(
                EquipmentType.valueOf(embeddable.getEquipmentType()),
                embeddable.getQuantity(),
                ConditionScore.of(embeddable.getMinimumConditionScore()),
                embeddable.getPreferredBrand(),
                embeddable.isPreferRecent()
        );
    }
}
