package com.tequipy.equipment.application.port.in;

import com.tequipy.equipment.domain.model.EquipmentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateAllocationCommandTest {

    @Test
    void shouldCreateCommandWhenAllFieldsValid() {
        // given
        var policyItemCommand = new CreateAllocationCommand.PolicyItemCommand(
                EquipmentType.MAIN_COMPUTER, 1, BigDecimal.valueOf(0.5), "Dell", true
        );

        // when
        var command = new CreateAllocationCommand("EMP-001", List.of(policyItemCommand));

        // then
        assertThat(command.employeeId()).isEqualTo("EMP-001");
        assertThat(command.policyItems()).hasSize(1);
        assertThat(command.policyItems().getFirst().equipmentType()).isEqualTo(EquipmentType.MAIN_COMPUTER);
    }

    @Test
    void shouldThrowWhenEmployeeIdNull() {
        var policyItemCommand = new CreateAllocationCommand.PolicyItemCommand(
                EquipmentType.MAIN_COMPUTER, 1, BigDecimal.valueOf(0.5), null, false
        );

        assertThatThrownBy(() -> new CreateAllocationCommand(null, List.of(policyItemCommand)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Employee id is required");
    }

    @Test
    void shouldThrowWhenPolicyItemsNull() {
        assertThatThrownBy(() -> new CreateAllocationCommand("EMP-001", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Policy items are required");
    }

    @Test
    void shouldCreateDefensiveCopyOfPolicyItems() {
        // given
        var policyItemCommand = new CreateAllocationCommand.PolicyItemCommand(
                EquipmentType.MAIN_COMPUTER, 1, BigDecimal.valueOf(0.5), null, false
        );
        var mutableList = new ArrayList<>(List.of(policyItemCommand));

        // when
        var command = new CreateAllocationCommand("EMP-001", mutableList);
        mutableList.clear();

        // then
        assertThat(command.policyItems()).hasSize(1);
    }

    @Test
    void shouldReturnImmutablePolicyItems() {
        // given
        var policyItemCommand = new CreateAllocationCommand.PolicyItemCommand(
                EquipmentType.MAIN_COMPUTER, 1, BigDecimal.valueOf(0.5), null, false
        );
        var command = new CreateAllocationCommand("EMP-001", List.of(policyItemCommand));

        // then
        assertThatThrownBy(() -> command.policyItems().add(policyItemCommand))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldCreatePolicyItemCommandWhenAllFieldsValid() {
        // when
        var policyItemCommand = new CreateAllocationCommand.PolicyItemCommand(
                EquipmentType.MONITOR, 2, BigDecimal.valueOf(0.8), "LG", true
        );

        // then
        assertThat(policyItemCommand.equipmentType()).isEqualTo(EquipmentType.MONITOR);
        assertThat(policyItemCommand.quantity()).isEqualTo(2);
        assertThat(policyItemCommand.minimumConditionScore()).isEqualByComparingTo(BigDecimal.valueOf(0.8));
        assertThat(policyItemCommand.preferredBrand()).isEqualTo("LG");
        assertThat(policyItemCommand.preferRecent()).isTrue();
    }

    @Test
    void shouldThrowWhenPolicyItemEquipmentTypeNull() {
        assertThatThrownBy(() -> new CreateAllocationCommand.PolicyItemCommand(
                null, 1, BigDecimal.valueOf(0.5), null, false
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Equipment type is required");
    }

    @Test
    void shouldThrowWhenPolicyItemMinimumConditionScoreNull() {
        assertThatThrownBy(() -> new CreateAllocationCommand.PolicyItemCommand(
                EquipmentType.MAIN_COMPUTER, 1, null, null, false
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Minimum condition score is required");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldThrowWhenPolicyItemQuantityLessThanOne(int invalidQuantity) {
        assertThatThrownBy(() -> new CreateAllocationCommand.PolicyItemCommand(
                EquipmentType.MAIN_COMPUTER, invalidQuantity, BigDecimal.valueOf(0.5), null, false
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be at least 1");
    }

    @Test
    void shouldAllowNullPreferredBrandInPolicyItemCommand() {
        // when
        var policyItemCommand = new CreateAllocationCommand.PolicyItemCommand(
                EquipmentType.KEYBOARD, 1, BigDecimal.valueOf(0.5), null, false
        );

        // then
        assertThat(policyItemCommand.preferredBrand()).isNull();
    }
}
