package com.tequipy.equipment.domain.model;

import com.tequipy.equipment.domain.exception.InvalidAllocationStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllocationRequestTest {

    @Test
    void shouldMarkAllocatedWhenPending() {
        // given
        var request = createAllocationRequest(AllocationState.PENDING);
        var equipmentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        // when
        request.markAllocated(equipmentIds);

        // then
        assertThat(request.state()).isEqualTo(AllocationState.ALLOCATED);
        assertThat(request.allocatedEquipmentIds()).containsExactlyElementsOf(equipmentIds);
    }

    @ParameterizedTest
    @EnumSource(value = AllocationState.class, names = {"ALLOCATED", "CONFIRMED", "CANCELLED", "FAILED"})
    void shouldThrowWhenMarkAllocatedFromNonPending(AllocationState initialState) {
        // given
        var request = createAllocationRequest(initialState);

        // then
        assertThatThrownBy(() -> request.markAllocated(List.of(UUID.randomUUID())))
                .isInstanceOf(InvalidAllocationStateTransitionException.class);
    }

    @Test
    void shouldConfirmWhenAllocated() {
        // given
        var request = createAllocationRequest(AllocationState.ALLOCATED);

        // when
        request.confirm();

        // then
        assertThat(request.state()).isEqualTo(AllocationState.CONFIRMED);
    }

    @ParameterizedTest
    @EnumSource(value = AllocationState.class, names = {"PENDING", "CONFIRMED", "CANCELLED", "FAILED"})
    void shouldThrowWhenConfirmingNonAllocatedRequest(AllocationState initialState) {
        // given
        var request = createAllocationRequest(initialState);

        // then
        assertThatThrownBy(request::confirm)
                .isInstanceOf(InvalidAllocationStateTransitionException.class);
    }

    @Test
    void shouldThrowWhenConfirmingPendingRequest() {
        // given
        var request = createAllocationRequest(AllocationState.PENDING);

        // then
        assertThatThrownBy(request::confirm)
                .isInstanceOf(InvalidAllocationStateTransitionException.class);
    }

    @Test
    void shouldCancelWhenAllocated() {
        // given
        var request = createAllocationRequest(AllocationState.ALLOCATED);

        // when
        request.cancel();

        // then
        assertThat(request.state()).isEqualTo(AllocationState.CANCELLED);
    }

    @Test
    void shouldCancelWhenPending() {
        // given
        var request = createAllocationRequest(AllocationState.PENDING);

        // when
        request.cancel();

        // then
        assertThat(request.state()).isEqualTo(AllocationState.CANCELLED);
    }

    @ParameterizedTest
    @EnumSource(value = AllocationState.class, names = {"CONFIRMED", "CANCELLED", "FAILED"})
    void shouldThrowWhenCancellingFromInvalidState(AllocationState initialState) {
        // given
        var request = createAllocationRequest(initialState);

        // then
        assertThatThrownBy(request::cancel)
                .isInstanceOf(InvalidAllocationStateTransitionException.class);
    }

    @Test
    void shouldThrowWhenCancellingConfirmed() {
        // given
        var request = createAllocationRequest(AllocationState.CONFIRMED);

        // then
        assertThatThrownBy(request::cancel)
                .isInstanceOf(InvalidAllocationStateTransitionException.class);
    }

    @Test
    void shouldFailWhenPending() {
        // given
        var request = createAllocationRequest(AllocationState.PENDING);

        // when
        request.fail();

        // then
        assertThat(request.state()).isEqualTo(AllocationState.FAILED);
    }

    @ParameterizedTest
    @EnumSource(value = AllocationState.class, names = {"ALLOCATED", "CONFIRMED", "CANCELLED", "FAILED"})
    void shouldThrowWhenFailingNonPendingRequest(AllocationState initialState) {
        // given
        var request = createAllocationRequest(initialState);

        // then
        assertThatThrownBy(request::fail)
                .isInstanceOf(InvalidAllocationStateTransitionException.class);
    }

    @Test
    void shouldThrowWhenFailingAllocated() {
        // given
        var request = createAllocationRequest(AllocationState.ALLOCATED);

        // then
        assertThatThrownBy(request::fail)
                .isInstanceOf(InvalidAllocationStateTransitionException.class);
    }

    private AllocationRequest createAllocationRequest(AllocationState state) {
        var policyItem = new PolicyItem(
                EquipmentType.MAIN_COMPUTER,
                1,
                ConditionScore.of(0.5),
                null,
                false
        );
        return new AllocationRequest(
                UUID.randomUUID(),
                "EMP-001",
                List.of(policyItem),
                state,
                state == AllocationState.ALLOCATED ? List.of(UUID.randomUUID()) : List.of()
        );
    }
}
