package com.tequipy.equipment.domain.event;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllocationCreatedEventTest {

    @Test
    void shouldCreateEventWhenAllocationRequestIdProvided() {
        // given
        var allocationRequestId = UUID.randomUUID();

        // when
        var event = new AllocationCreatedEvent(allocationRequestId);

        // then
        assertThat(event.allocationRequestId()).isEqualTo(allocationRequestId);
    }

    @Test
    void shouldThrowWhenAllocationRequestIdNull() {
        assertThatThrownBy(() -> new AllocationCreatedEvent(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Allocation request id is required");
    }

    @Test
    void shouldSupportRecordEquality() {
        // given
        var allocationRequestId = UUID.randomUUID();
        var eventA = new AllocationCreatedEvent(allocationRequestId);
        var eventB = new AllocationCreatedEvent(allocationRequestId);

        // then
        assertThat(eventA).isEqualTo(eventB);
        assertThat(eventA.hashCode()).isEqualTo(eventB.hashCode());
    }
}
