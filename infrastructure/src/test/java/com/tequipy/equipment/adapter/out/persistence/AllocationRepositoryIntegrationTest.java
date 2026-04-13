package com.tequipy.equipment.adapter.out.persistence;

import com.tequipy.equipment.adapter.out.persistence.entity.AllocationRequestJpaEntity;
import com.tequipy.equipment.adapter.out.persistence.entity.EquipmentJpaEntity;
import com.tequipy.equipment.adapter.out.persistence.entity.PolicyItemEmbeddable;
import com.tequipy.equipment.adapter.out.persistence.repository.AllocationRequestJpaRepository;
import com.tequipy.equipment.adapter.out.persistence.repository.EquipmentJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AllocationRepositoryIntegrationTest {

    @Autowired
    private AllocationRequestJpaRepository allocationRequestJpaRepository;

    @Autowired
    private EquipmentJpaRepository equipmentJpaRepository;

    @Test
    void shouldPersistAndRetrieveAllocationRequestById() {
        // given
        var entity = createAllocationEntity(UUID.randomUUID(), "PENDING");
        allocationRequestJpaRepository.save(entity);

        // when
        var result = allocationRequestJpaRepository.findById(entity.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmployeeId()).isEqualTo("EMP-001");
        assertThat(result.get().getState()).isEqualTo("PENDING");
    }

    @Test
    void shouldReturnEmptyWhenAllocationNotFound() {
        // given
        var nonexistentId = UUID.randomUUID();

        // when
        var result = allocationRequestJpaRepository.findById(nonexistentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldPersistAllocationWithPolicyItems() {
        // given
        var entity = createAllocationEntity(UUID.randomUUID(), "PENDING");
        var policyItem = new PolicyItemEmbeddable("MAIN_COMPUTER", 1, BigDecimal.valueOf(0.5), "Dell", true);
        entity.setPolicyItems(new ArrayList<>(List.of(policyItem)));
        allocationRequestJpaRepository.save(entity);

        // when
        var result = allocationRequestJpaRepository.findById(entity.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPolicyItems()).hasSize(1);
        assertThat(result.get().getPolicyItems().getFirst().getEquipmentType()).isEqualTo("MAIN_COMPUTER");
        assertThat(result.get().getPolicyItems().getFirst().getQuantity()).isEqualTo(1);
        assertThat(result.get().getPolicyItems().getFirst().getPreferredBrand()).isEqualTo("Dell");
    }

    @Test
    void shouldPersistAllocationWithAllocatedEquipmentIds() {
        // given
        var equipmentId = UUID.randomUUID();
        var equipmentEntity = new EquipmentJpaEntity(equipmentId, "MAIN_COMPUTER", "Dell", "Latitude 5540",
                "RESERVED", BigDecimal.valueOf(0.90), LocalDate.of(2024, 6, 1), null);
        equipmentJpaRepository.save(equipmentEntity);

        var allocationEntity = createAllocationEntity(UUID.randomUUID(), "ALLOCATED");
        allocationEntity.setAllocatedEquipmentIds(new ArrayList<>(List.of(equipmentId)));
        allocationRequestJpaRepository.save(allocationEntity);

        // when
        var result = allocationRequestJpaRepository.findById(allocationEntity.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getAllocatedEquipmentIds()).containsExactly(equipmentId);
    }

    @Test
    void shouldUpdateAllocationState() {
        // given
        var entity = createAllocationEntity(UUID.randomUUID(), "PENDING");
        allocationRequestJpaRepository.save(entity);

        // when
        entity.setState("ALLOCATED");
        allocationRequestJpaRepository.save(entity);

        // then
        var updated = allocationRequestJpaRepository.findById(entity.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getState()).isEqualTo("ALLOCATED");
    }

    @Test
    void shouldPersistMultiplePolicyItems() {
        // given
        var entity = createAllocationEntity(UUID.randomUUID(), "PENDING");
        var computerPolicy = new PolicyItemEmbeddable("MAIN_COMPUTER", 1, BigDecimal.valueOf(0.7), "Dell", true);
        var monitorPolicy = new PolicyItemEmbeddable("MONITOR", 2, BigDecimal.valueOf(0.5), null, false);
        entity.setPolicyItems(new ArrayList<>(List.of(computerPolicy, monitorPolicy)));
        allocationRequestJpaRepository.save(entity);

        // when
        var result = allocationRequestJpaRepository.findById(entity.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPolicyItems()).hasSize(2);
    }

    private AllocationRequestJpaEntity createAllocationEntity(UUID id, String state) {
        return new AllocationRequestJpaEntity(id, "EMP-001", state, new ArrayList<>(), new ArrayList<>());
    }
}
