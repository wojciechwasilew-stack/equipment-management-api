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

class AllocationAlgorithmTest {

    @Test
    void shouldAllocateOptimalEquipmentWhenMultipleCandidatesCompete() {
        // given
        var laptop1 = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.9, LocalDate.of(2024, 6, 1));
        var laptop2 = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.7, LocalDate.of(2023, 1, 1));
        var monitor1 = createEquipment(EquipmentType.MONITOR, "LG", 0.8, LocalDate.of(2024, 3, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false),
                new PolicyItem(EquipmentType.MONITOR, 1, ConditionScore.of(0.5), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(laptop1, laptop2, monitor1));

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Equipment::type)
                .containsExactlyInAnyOrder(EquipmentType.MAIN_COMPUTER, EquipmentType.MONITOR);
    }

    @Test
    void shouldReturnEmptyWhenNoEquipmentSatisfiesHardConstraints() {
        // given
        var lowConditionLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.3, LocalDate.of(2023, 1, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.8), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(lowConditionLaptop));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldPreferBrandMatchWhenSoftPreferenceSet() {
        // given
        var dellLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.8, LocalDate.of(2024, 1, 1));
        var hpLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, "HP", 0.8, LocalDate.of(2024, 1, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), "Dell", false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(hpLaptop, dellLaptop));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().brand()).isEqualTo("Dell");
    }

    @Test
    void shouldHandleGlobalConflictWhenEquipmentCompetesAcrossSlots() {
        // given
        var sharedLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, "Dell", 0.9, LocalDate.of(2024, 1, 1));
        var fallbackLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, "HP", 0.7, LocalDate.of(2023, 1, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 2, ConditionScore.of(0.5), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(sharedLaptop, fallbackLaptop));

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Equipment::id)
                .doesNotHaveDuplicates();
    }

    @Test
    void shouldReturnEmptyWhenNoAvailableEquipment() {
        // given
        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldAllocateMultipleItemsWhenQuantityGreaterThanOne() {
        // given
        var monitor1 = createEquipment(EquipmentType.MONITOR, "LG", 0.9, LocalDate.of(2024, 1, 1));
        var monitor2 = createEquipment(EquipmentType.MONITOR, "Samsung", 0.8, LocalDate.of(2024, 2, 1));
        var monitor3 = createEquipment(EquipmentType.MONITOR, "Dell", 0.7, LocalDate.of(2024, 3, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MONITOR, 3, ConditionScore.of(0.5), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(monitor1, monitor2, monitor3));

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Equipment::id).doesNotHaveDuplicates();
        assertThat(result).allMatch(e -> e.type() == EquipmentType.MONITOR);
    }

    @Test
    void shouldReturnEmptyWhenEquipmentTypeDoesNotMatch() {
        // given
        var monitor = createEquipment(EquipmentType.MONITOR, "LG", 0.9, LocalDate.of(2024, 1, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(monitor));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldExcludeNonAvailableEquipmentFromCandidates() {
        // given
        var reservedLaptop = new Equipment(
                UUID.randomUUID(),
                EquipmentType.MAIN_COMPUTER,
                "Dell",
                "Latitude 5540",
                EquipmentState.RESERVED,
                ConditionScore.of(0.9),
                LocalDate.of(2024, 1, 1),
                null
        );

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(reservedLaptop));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNotEnoughCandidatesForQuantity() {
        // given
        var monitor = createEquipment(EquipmentType.MONITOR, "LG", 0.9, LocalDate.of(2024, 1, 1));

        var policyItems = List.of(
                new PolicyItem(EquipmentType.MONITOR, 3, ConditionScore.of(0.5), null, false)
        );

        // when
        var result = AllocationAlgorithm.allocate(policyItems, List.of(monitor));

        // then
        assertThat(result).isEmpty();
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
