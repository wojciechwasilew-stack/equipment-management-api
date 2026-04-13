package com.tequipy.equipment.adapter.out.persistence;

import com.tequipy.equipment.adapter.out.persistence.entity.EquipmentJpaEntity;
import com.tequipy.equipment.adapter.out.persistence.mapper.AllocationPersistenceMapperImpl;
import com.tequipy.equipment.adapter.out.persistence.mapper.EquipmentPersistenceMapperImpl;
import com.tequipy.equipment.adapter.out.persistence.repository.AllocationRequestJpaRepository;
import com.tequipy.equipment.adapter.out.persistence.repository.EquipmentJpaRepository;
import com.tequipy.equipment.domain.model.AllocationRequest;
import com.tequipy.equipment.domain.model.AllocationState;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.EquipmentType;
import com.tequipy.equipment.domain.model.PolicyItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({AllocationPersistenceAdapter.class, AllocationPersistenceMapperImpl.class,
        EquipmentPersistenceAdapter.class, EquipmentPersistenceMapperImpl.class})
@ActiveProfiles("test")
class AllocationPersistenceAdapterIntegrationTest {

    @Autowired
    private AllocationPersistenceAdapter allocationPersistenceAdapter;

    @Autowired
    private AllocationRequestJpaRepository allocationRequestJpaRepository;

    @Autowired
    private EquipmentJpaRepository equipmentJpaRepository;

    @BeforeEach
    void setUp() {
        allocationRequestJpaRepository.deleteAll();
        equipmentJpaRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveAllocationRequestWithPolicyItems() {
        // given
        var allocationId = UUID.randomUUID();
        var policyItem = new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), "Dell", true);
        var allocation = new AllocationRequest(allocationId, "EMP-001", List.of(policyItem),
                AllocationState.PENDING, List.of());

        // when
        allocationPersistenceAdapter.save(allocation);
        var result = allocationPersistenceAdapter.findById(allocationId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(allocationId);
        assertThat(result.get().employeeId()).isEqualTo("EMP-001");
        assertThat(result.get().state()).isEqualTo(AllocationState.PENDING);
        assertThat(result.get().policyItems()).hasSize(1);
        assertThat(result.get().policyItems().getFirst().equipmentType()).isEqualTo(EquipmentType.MAIN_COMPUTER);
        assertThat(result.get().policyItems().getFirst().quantity()).isEqualTo(1);
        assertThat(result.get().policyItems().getFirst().preferredBrand()).isEqualTo("Dell");
        assertThat(result.get().policyItems().getFirst().preferRecent()).isTrue();
    }

    @Test
    void shouldReturnEmptyOptionalWhenAllocationNotFound() {
        // given
        var nonexistentId = UUID.randomUUID();

        // when
        var result = allocationPersistenceAdapter.findById(nonexistentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldPersistMultiplePolicyItemsWithDifferentTypes() {
        // given
        var allocationId = UUID.randomUUID();
        var computerPolicy = new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.7), "Dell", true);
        var monitorPolicy = new PolicyItem(EquipmentType.MONITOR, 2, ConditionScore.of(0.5), null, false);
        var keyboardPolicy = new PolicyItem(EquipmentType.KEYBOARD, 1, ConditionScore.of(0.3), "Logitech", false);
        var allocation = new AllocationRequest(allocationId, "EMP-002",
                List.of(computerPolicy, monitorPolicy, keyboardPolicy),
                AllocationState.PENDING, List.of());

        // when
        allocationPersistenceAdapter.save(allocation);
        var result = allocationPersistenceAdapter.findById(allocationId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().policyItems()).hasSize(3);
        assertThat(result.get().policyItems())
                .extracting(PolicyItem::equipmentType)
                .containsExactlyInAnyOrder(EquipmentType.MAIN_COMPUTER, EquipmentType.MONITOR, EquipmentType.KEYBOARD);
    }

    @Test
    void shouldPreserveStateTransitionFromPendingToAllocated() {
        // given
        var allocationId = UUID.randomUUID();
        var equipmentId = persistEquipmentEntity(UUID.randomUUID(), "RESERVED");
        var policyItem = new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), "Dell", true);
        var allocation = new AllocationRequest(allocationId, "EMP-001", List.of(policyItem),
                AllocationState.PENDING, List.of());
        allocationPersistenceAdapter.save(allocation);

        // when
        allocation.markAllocated(List.of(equipmentId));
        allocationPersistenceAdapter.save(allocation);

        // then
        var loaded = allocationPersistenceAdapter.findById(allocationId);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().state()).isEqualTo(AllocationState.ALLOCATED);
        assertThat(loaded.get().allocatedEquipmentIds()).containsExactly(equipmentId);
    }

    @Test
    void shouldPreserveStateTransitionFromAllocatedToConfirmed() {
        // given
        var allocationId = UUID.randomUUID();
        var equipmentId = persistEquipmentEntity(UUID.randomUUID(), "RESERVED");
        var policyItem = new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), "Dell", true);
        var allocation = new AllocationRequest(allocationId, "EMP-001", List.of(policyItem),
                AllocationState.PENDING, List.of());
        allocationPersistenceAdapter.save(allocation);
        allocation.markAllocated(List.of(equipmentId));
        allocationPersistenceAdapter.save(allocation);

        // when
        allocation.confirm();
        allocationPersistenceAdapter.save(allocation);

        // then
        var loaded = allocationPersistenceAdapter.findById(allocationId);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().state()).isEqualTo(AllocationState.CONFIRMED);
    }

    @Test
    void shouldPreserveStateTransitionFromPendingToCancelled() {
        // given
        var allocationId = UUID.randomUUID();
        var policyItem = new PolicyItem(EquipmentType.MONITOR, 1, ConditionScore.of(0.3), null, false);
        var allocation = new AllocationRequest(allocationId, "EMP-003", List.of(policyItem),
                AllocationState.PENDING, List.of());
        allocationPersistenceAdapter.save(allocation);

        // when
        allocation.cancel();
        allocationPersistenceAdapter.save(allocation);

        // then
        var loaded = allocationPersistenceAdapter.findById(allocationId);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().state()).isEqualTo(AllocationState.CANCELLED);
    }

    @Test
    void shouldPreserveStateTransitionFromPendingToFailed() {
        // given
        var allocationId = UUID.randomUUID();
        var policyItem = new PolicyItem(EquipmentType.MOUSE, 1, ConditionScore.of(0.9), "Logitech", true);
        var allocation = new AllocationRequest(allocationId, "EMP-004", List.of(policyItem),
                AllocationState.PENDING, List.of());
        allocationPersistenceAdapter.save(allocation);

        // when
        allocation.fail();
        allocationPersistenceAdapter.save(allocation);

        // then
        var loaded = allocationPersistenceAdapter.findById(allocationId);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().state()).isEqualTo(AllocationState.FAILED);
    }

    @Test
    void shouldPersistAllocationWithMultipleAllocatedEquipmentIds() {
        // given
        var equipmentIdOne = persistEquipmentEntity(UUID.randomUUID(), "RESERVED");
        var equipmentIdTwo = persistEquipmentEntity(UUID.randomUUID(), "RESERVED");
        var allocationId = UUID.randomUUID();
        var computerPolicy = new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false);
        var monitorPolicy = new PolicyItem(EquipmentType.MONITOR, 1, ConditionScore.of(0.5), null, false);
        var allocation = new AllocationRequest(allocationId, "EMP-005",
                List.of(computerPolicy, monitorPolicy), AllocationState.PENDING, List.of());
        allocationPersistenceAdapter.save(allocation);

        // when
        allocation.markAllocated(List.of(equipmentIdOne, equipmentIdTwo));
        allocationPersistenceAdapter.save(allocation);

        // then
        var loaded = allocationPersistenceAdapter.findById(allocationId);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().allocatedEquipmentIds())
                .containsExactlyInAnyOrder(equipmentIdOne, equipmentIdTwo);
    }

    @Test
    void shouldPreserveConditionScorePrecisionThroughPersistence() {
        // given
        var allocationId = UUID.randomUUID();
        var policyItem = new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(new BigDecimal("0.75")), "Dell", true);
        var allocation = new AllocationRequest(allocationId, "EMP-006", List.of(policyItem),
                AllocationState.PENDING, List.of());

        // when
        allocationPersistenceAdapter.save(allocation);
        var loaded = allocationPersistenceAdapter.findById(allocationId);

        // then
        assertThat(loaded).isPresent();
        assertThat(loaded.get().policyItems().getFirst().minimumConditionScore().value())
                .isEqualByComparingTo("0.75");
    }

    private UUID persistEquipmentEntity(UUID equipmentId, String state) {
        var entity = new EquipmentJpaEntity(
                equipmentId, "MAIN_COMPUTER", "Dell", "Latitude 5540",
                state, BigDecimal.valueOf(0.90), LocalDate.of(2024, 6, 1), null
        );
        equipmentJpaRepository.save(entity);
        return equipmentId;
    }
}
