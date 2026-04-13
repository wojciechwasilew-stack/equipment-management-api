package com.tequipy.equipment.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyItemTest {

    @Test
    void shouldCreatePolicyItemWhenAllFieldsValid() {
        // when
        var policyItem = new PolicyItem(
                EquipmentType.MAIN_COMPUTER, 2, ConditionScore.of(0.8), "Dell", true
        );

        // then
        assertThat(policyItem.equipmentType()).isEqualTo(EquipmentType.MAIN_COMPUTER);
        assertThat(policyItem.quantity()).isEqualTo(2);
        assertThat(policyItem.minimumConditionScore()).isEqualTo(ConditionScore.of(0.8));
        assertThat(policyItem.preferredBrand()).isEqualTo("Dell");
        assertThat(policyItem.preferRecent()).isTrue();
    }

    @Test
    void shouldCreatePolicyItemWhenPreferredBrandNull() {
        // when
        var policyItem = new PolicyItem(
                EquipmentType.MONITOR, 1, ConditionScore.of(0.5), null, false
        );

        // then
        assertThat(policyItem.preferredBrand()).isNull();
    }

    @Test
    void shouldThrowWhenEquipmentTypeNull() {
        assertThatThrownBy(() -> new PolicyItem(null, 1, ConditionScore.of(0.5), null, false))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Equipment type is required");
    }

    @Test
    void shouldThrowWhenMinimumConditionScoreNull() {
        assertThatThrownBy(() -> new PolicyItem(EquipmentType.MONITOR, 1, null, null, false))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Minimum condition score is required");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100, Integer.MIN_VALUE})
    void shouldThrowWhenQuantityLessThanOne(int invalidQuantity) {
        assertThatThrownBy(() -> new PolicyItem(EquipmentType.MONITOR, invalidQuantity, ConditionScore.of(0.5), null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be at least 1");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 10, 100})
    void shouldAcceptValidQuantity(int validQuantity) {
        // when
        var policyItem = new PolicyItem(EquipmentType.MONITOR, validQuantity, ConditionScore.of(0.5), null, false);

        // then
        assertThat(policyItem.quantity()).isEqualTo(validQuantity);
    }
}
