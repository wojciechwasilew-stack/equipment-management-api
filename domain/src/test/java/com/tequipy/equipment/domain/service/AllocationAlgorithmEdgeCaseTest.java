package com.tequipy.equipment.domain.service;

import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;
import com.tequipy.equipment.domain.model.EquipmentType;
import com.tequipy.equipment.domain.model.PolicyItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationAlgorithmEdgeCaseTest {

    @Test
    void shouldPreferRecentEquipmentWhenPreferRecentEnabled() {
        // given
        var olderLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.8, LocalDate.of(2020, 1, 1));
        var newerLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.8, LocalDate.of(2025, 6, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, true)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(olderLaptop, newerLaptop));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(newerLaptop.id());
    }

    @Test
    void shouldMatchBrandCaseInsensitively() {
        // given
        var dellLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, "DELL", 0.8, LocalDate.of(2024, 1, 1));
        var hpLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, "HP", 0.8, LocalDate.of(2024, 1, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), "dell", false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(hpLaptop, dellLaptop));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().brand()).isEqualTo("DELL");
    }

    @Test
    void shouldAllocateAcrossMultiplePolicyItemTypes() {
        // given
        var laptop = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.9, LocalDate.of(2024, 1, 1));
        var monitor = createEquipment(EquipmentType.MONITOR, "LG", 0.8, LocalDate.of(2024, 1, 1));
        var keyboard = createEquipment(EquipmentType.KEYBOARD, "Logitech", 0.7, LocalDate.of(2024, 1, 1));
        var mouse = createEquipment(EquipmentType.MOUSE, "Logitech", 0.7, LocalDate.of(2024, 1, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false),
                new PolicyItem(EquipmentType.MONITOR, 1, ConditionScore.of(0.5), null, false),
                new PolicyItem(EquipmentType.KEYBOARD, 1, ConditionScore.of(0.5), null, false),
                new PolicyItem(EquipmentType.MOUSE, 1, ConditionScore.of(0.5), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(laptop, monitor, keyboard, mouse));

        // then
        assertThat(result).hasSize(4);
        assertThat(result).extracting(Equipment::id).doesNotHaveDuplicates();
    }

    @Test
    void shouldReturnEmptyWhenPolicyItemsEmpty() {
        // given
        var laptop = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.9, LocalDate.of(2024, 1, 1));

        // when
        var result = AllocationAlgorithm.allocate(List.of(), List.of(laptop));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenBothListsEmpty() {
        // when
        var result = AllocationAlgorithm.allocate(List.of(), List.of());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFilterByExactConditionScoreThreshold() {
        // given
        var exactMatchEquipment = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.8, LocalDate.of(2024, 1, 1));
        var belowThresholdEquipment = createEquipment(EquipmentType.MAIN_COMPUTER, "HP", 0.79, LocalDate.of(2024, 1, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.8), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(belowThresholdEquipment, exactMatchEquipment));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(exactMatchEquipment.id());
    }

    @Test
    void shouldBacktrackWhenFirstChoiceLeadsToDeadEnd() {
        // given
        var sharedMonitor = createEquipment(EquipmentType.MONITOR, "LG", 0.95, LocalDate.of(2024, 1, 1));
        var exclusiveMonitor = createEquipment(EquipmentType.MONITOR, "LG", 0.6, LocalDate.of(2024, 1, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MONITOR, 1, ConditionScore.of(0.9), null, false),
                new PolicyItem(EquipmentType.MONITOR, 1, ConditionScore.of(0.5), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(sharedMonitor, exclusiveMonitor));

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Equipment::id).doesNotHaveDuplicates();
    }

    @Test
    void shouldPreferHigherConditionScoreEquipment() {
        // given
        var lowConditionLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.6, LocalDate.of(2024, 1, 1));
        var highConditionLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.95, LocalDate.of(2024, 1, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(lowConditionLaptop, highConditionLaptop));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(highConditionLaptop.id());
    }

    @Test
    void shouldReturnImmutableResult() {
        // given
        var laptop = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.9, LocalDate.of(2024, 1, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(laptop));

        // then
        assertThat(result).hasSize(1);
        assertThat(result).isUnmodifiable();
    }

    private Equipment createEquipment(EquipmentType type, String brand, double conditionScore, LocalDate purchaseDate) {
        return new Equipment(
                UUID.randomUUID(),
                type,
                brand,
                "Model-X",
                EquipmentState.AVAILABLE,
                ConditionScore.of(conditionScore),
                purchaseDate,
                null
        );
    }
}
