package com.tequipy.equipment.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllocationRequestConstructionTest {

    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final String EMPLOYEE_ID = "EMP-001";
    private static final PolicyItem DEFAULT_POLICY_ITEM = new PolicyItem(
            EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false
    );

    @Test
    void shouldCreateAllocationRequestWhenAllFieldsValid() {
        // when
        var request = new AllocationRequest(
                REQUEST_ID, EMPLOYEE_ID, List.of(DEFAULT_POLICY_ITEM), AllocationState.PENDING, List.of()
        );

        // then
        assertThat(request.id()).isEqualTo(REQUEST_ID);
        assertThat(request.employeeId()).isEqualTo(EMPLOYEE_ID);
        assertThat(request.policyItems()).containsExactly(DEFAULT_POLICY_ITEM);
        assertThat(request.state()).isEqualTo(AllocationState.PENDING);
        assertThat(request.allocatedEquipmentIds()).isEmpty();
    }

    @Test
    void shouldThrowWhenIdNull() {
        assertThatThrownBy(() -> new AllocationRequest(null, EMPLOYEE_ID, List.of(DEFAULT_POLICY_ITEM), AllocationState.PENDING, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Allocation request id is required");
    }

    @Test
    void shouldThrowWhenEmployeeIdNull() {
        assertThatThrownBy(() -> new AllocationRequest(REQUEST_ID, null, List.of(DEFAULT_POLICY_ITEM), AllocationState.PENDING, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Employee id is required");
    }

    @Test
    void shouldThrowWhenPolicyItemsNull() {
        assertThatThrownBy(() -> new AllocationRequest(REQUEST_ID, EMPLOYEE_ID, null, AllocationState.PENDING, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Policy items are required");
    }

    @Test
    void shouldThrowWhenStateNull() {
        assertThatThrownBy(() -> new AllocationRequest(REQUEST_ID, EMPLOYEE_ID, List.of(DEFAULT_POLICY_ITEM), null, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("State is required");
    }

    @Test
    void shouldDefaultToEmptyListWhenAllocatedEquipmentIdsNull() {
        // when
        var request = new AllocationRequest(
                REQUEST_ID, EMPLOYEE_ID, List.of(DEFAULT_POLICY_ITEM), AllocationState.PENDING, null
        );

        // then
        assertThat(request.allocatedEquipmentIds()).isEmpty();
    }

    @Test
    void shouldCreateDefensiveCopyOfPolicyItems() {
        // given
        var mutableList = new ArrayList<>(List.of(DEFAULT_POLICY_ITEM));

        // when
        var request = new AllocationRequest(REQUEST_ID, EMPLOYEE_ID, mutableList, AllocationState.PENDING, List.of());
        mutableList.clear();

        // then
        assertThat(request.policyItems()).hasSize(1);
    }

    @Test
    void shouldReturnImmutablePolicyItems() {
        // given
        var request = new AllocationRequest(
                REQUEST_ID, EMPLOYEE_ID, List.of(DEFAULT_POLICY_ITEM), AllocationState.PENDING, List.of()
        );

        // then
        assertThatThrownBy(() -> request.policyItems().add(DEFAULT_POLICY_ITEM))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldReturnImmutableAllocatedEquipmentIds() {
        // given
        var equipmentId = UUID.randomUUID();
        var request = new AllocationRequest(
                REQUEST_ID, EMPLOYEE_ID, List.of(DEFAULT_POLICY_ITEM), AllocationState.ALLOCATED, List.of(equipmentId)
        );

        // then
        assertThatThrownBy(() -> request.allocatedEquipmentIds().add(UUID.randomUUID()))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
