package com.tequipy.equipment.domain.exception;

import com.tequipy.equipment.domain.model.AllocationState;
import com.tequipy.equipment.domain.model.EquipmentState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionHierarchyTest {

    @Test
    void shouldCreateEquipmentNotFoundExceptionWithMessage() {
        // given
        var equipmentId = UUID.randomUUID();

        // when
        var exception = new EquipmentNotFoundException(equipmentId);

        // then
        assertThat(exception)
                .isInstanceOf(DomainException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(equipmentId.toString())
                .hasMessageContaining("Equipment not found");
    }

    @Test
    void shouldCreateAllocationNotFoundExceptionWithMessage() {
        // given
        var allocationId = UUID.randomUUID();

        // when
        var exception = new AllocationNotFoundException(allocationId);

        // then
        assertThat(exception)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(allocationId.toString())
                .hasMessageContaining("Allocation request not found");
    }

    @Test
    void shouldCreateAllocationFailedExceptionWithMessage() {
        // given
        var allocationRequestId = UUID.randomUUID();

        // when
        var exception = new AllocationFailedException(allocationRequestId);

        // then
        assertThat(exception)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(allocationRequestId.toString())
                .hasMessageContaining("Allocation failed");
    }

    @Test
    void shouldCreateInvalidEquipmentStateTransitionExceptionWithMessage() {
        // given
        var equipmentId = UUID.randomUUID();

        // when
        var exception = new InvalidEquipmentStateTransitionException(
                equipmentId, EquipmentState.AVAILABLE, EquipmentState.ASSIGNED
        );

        // then
        assertThat(exception)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(equipmentId.toString())
                .hasMessageContaining("AVAILABLE")
                .hasMessageContaining("ASSIGNED");
    }

    @Test
    void shouldCreateInvalidAllocationStateTransitionExceptionWithMessage() {
        // given
        var allocationId = UUID.randomUUID();

        // when
        var exception = new InvalidAllocationStateTransitionException(
                allocationId, AllocationState.PENDING, AllocationState.CONFIRMED
        );

        // then
        assertThat(exception)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(allocationId.toString())
                .hasMessageContaining("PENDING")
                .hasMessageContaining("CONFIRMED");
    }
}
