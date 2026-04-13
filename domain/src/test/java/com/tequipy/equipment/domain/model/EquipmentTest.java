package com.tequipy.equipment.domain.model;

import com.tequipy.equipment.domain.exception.InvalidEquipmentStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EquipmentTest {

    @Test
    void shouldReserveWhenAvailable() {
        // given
        var equipment = createEquipment(EquipmentState.AVAILABLE);

        // when
        equipment.reserve();

        // then
        assertThat(equipment.state()).isEqualTo(EquipmentState.RESERVED);
    }

    @ParameterizedTest
    @EnumSource(value = EquipmentState.class, names = {"RESERVED", "ASSIGNED", "RETIRED"})
    void shouldThrowWhenReservingNonAvailableEquipment(EquipmentState initialState) {
        // given
        var equipment = createEquipment(initialState);

        // then
        assertThatThrownBy(equipment::reserve)
                .isInstanceOf(InvalidEquipmentStateTransitionException.class);
    }

    @Test
    void shouldThrowWhenReservingRetiredEquipment() {
        // given
        var equipment = createEquipment(EquipmentState.RETIRED);

        // then
        assertThatThrownBy(equipment::reserve)
                .isInstanceOf(InvalidEquipmentStateTransitionException.class);
    }

    @Test
    void shouldAssignWhenReserved() {
        // given
        var equipment = createEquipment(EquipmentState.RESERVED);

        // when
        equipment.assign();

        // then
        assertThat(equipment.state()).isEqualTo(EquipmentState.ASSIGNED);
    }

    @ParameterizedTest
    @EnumSource(value = EquipmentState.class, names = {"AVAILABLE", "ASSIGNED", "RETIRED"})
    void shouldThrowWhenAssigningNonReservedEquipment(EquipmentState initialState) {
        // given
        var equipment = createEquipment(initialState);

        // then
        assertThatThrownBy(equipment::assign)
                .isInstanceOf(InvalidEquipmentStateTransitionException.class);
    }

    @Test
    void shouldThrowWhenAssigningAvailableEquipment() {
        // given
        var equipment = createEquipment(EquipmentState.AVAILABLE);

        // then
        assertThatThrownBy(equipment::assign)
                .isInstanceOf(InvalidEquipmentStateTransitionException.class);
    }

    @Test
    void shouldReleaseWhenReserved() {
        // given
        var equipment = createEquipment(EquipmentState.RESERVED);

        // when
        equipment.release();

        // then
        assertThat(equipment.state()).isEqualTo(EquipmentState.AVAILABLE);
    }

    @Test
    void shouldReleaseWhenAssigned() {
        // given
        var equipment = createEquipment(EquipmentState.ASSIGNED);

        // when
        equipment.release();

        // then
        assertThat(equipment.state()).isEqualTo(EquipmentState.AVAILABLE);
    }

    @ParameterizedTest
    @EnumSource(value = EquipmentState.class, names = {"AVAILABLE", "RETIRED"})
    void shouldThrowWhenReleasingFromInvalidState(EquipmentState initialState) {
        // given
        var equipment = createEquipment(initialState);

        // then
        assertThatThrownBy(equipment::release)
                .isInstanceOf(InvalidEquipmentStateTransitionException.class);
    }

    @ParameterizedTest
    @EnumSource(value = EquipmentState.class, names = {"AVAILABLE", "RESERVED", "ASSIGNED"})
    void shouldRetireFromAnyNonRetiredState(EquipmentState initialState) {
        // given
        var equipment = createEquipment(initialState);

        // when
        equipment.retire("End of life");

        // then
        assertThat(equipment.state()).isEqualTo(EquipmentState.RETIRED);
        assertThat(equipment.retireReason()).isEqualTo("End of life");
    }

    @Test
    void shouldThrowWhenRetiringAlreadyRetired() {
        // given
        var equipment = createEquipment(EquipmentState.RETIRED);

        // then
        assertThatThrownBy(() -> equipment.retire("Duplicate retire"))
                .isInstanceOf(InvalidEquipmentStateTransitionException.class);
    }

    @Test
    void shouldThrowWhenRetireReasonNull() {
        // given
        var equipment = createEquipment(EquipmentState.AVAILABLE);

        // then
        assertThatThrownBy(() -> equipment.retire(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Retire reason is required");
    }

    private Equipment createEquipment(EquipmentState state) {
        return new Equipment(
                UUID.randomUUID(),
                EquipmentType.MAIN_COMPUTER,
                "Dell",
                "Latitude 5540",
                state,
                ConditionScore.of(0.9),
                LocalDate.of(2024, 1, 15),
                state == EquipmentState.RETIRED ? "Legacy reason" : null
        );
    }
}
