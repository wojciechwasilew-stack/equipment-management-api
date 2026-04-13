package com.tequipy.equipment.domain.service;

import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;
import com.tequipy.equipment.domain.model.PolicyItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AllocationAlgorithm {

    private AllocationAlgorithm() {
    }

    public static List<Equipment> allocate(List<PolicyItem> policyItems, List<Equipment> availableEquipment) {
        List<Slot> expandedSlots = expandPolicyItemsToSlots(policyItems);
        List<List<Equipment>> candidatesPerSlot = buildCandidateLists(expandedSlots, availableEquipment);
        List<IndexedSlot> indexedSlots = createSortedIndexedSlots(candidatesPerSlot);

        List<Equipment> result = new ArrayList<>();
        Set<UUID> usedEquipmentIds = new HashSet<>();

        if (backtrack(indexedSlots, candidatesPerSlot, usedEquipmentIds, result, 0)) {
            return List.copyOf(result);
        }

        return List.of();
    }

    private static List<Slot> expandPolicyItemsToSlots(List<PolicyItem> policyItems) {
        List<Slot> slots = new ArrayList<>();
        for (PolicyItem item : policyItems) {
            for (int i = 0; i < item.quantity(); i++) {
                slots.add(new Slot(item));
            }
        }
        return slots;
    }

    private static List<List<Equipment>> buildCandidateLists(List<Slot> slots, List<Equipment> available) {
        return slots.stream()
                .map(slot -> available.stream()
                        .filter(equipment -> matchesHardConstraints(equipment, slot.policyItem()))
                        .sorted(candidateComparator(slot.policyItem()))
                        .toList())
                .toList();
    }

    private static boolean matchesHardConstraints(Equipment equipment, PolicyItem policyItem) {
        return equipment.state() == EquipmentState.AVAILABLE
                && equipment.type() == policyItem.equipmentType()
                && equipment.conditionScore().isGreaterThanOrEqual(policyItem.minimumConditionScore());
    }

    private static Comparator<Equipment> candidateComparator(PolicyItem policyItem) {
        return Comparator.comparingDouble((Equipment e) -> -scoreSoftPreferences(e, policyItem));
    }

    private static double scoreSoftPreferences(Equipment equipment, PolicyItem policyItem) {
        double score = 0.0;

        if (policyItem.preferredBrand() != null
                && policyItem.preferredBrand().equalsIgnoreCase(equipment.brand())) {
            score += 10.0;
        }

        if (policyItem.preferRecent()) {
            long daysSincePurchase = LocalDate.now().toEpochDay() - equipment.purchaseDate().toEpochDay();
            score += Math.max(0, 5.0 - (daysSincePurchase / 365.0));
        }

        score += equipment.conditionScore().value().doubleValue();

        return score;
    }

    private static List<IndexedSlot> createSortedIndexedSlots(List<List<Equipment>> candidatesPerSlot) {
        List<IndexedSlot> indexed = new ArrayList<>();
        for (int i = 0; i < candidatesPerSlot.size(); i++) {
            indexed.add(new IndexedSlot(i, candidatesPerSlot.get(i).size()));
        }
        indexed.sort(Comparator.comparingInt(IndexedSlot::candidateCount));
        return indexed;
    }

    private static boolean backtrack(List<IndexedSlot> indexedSlots, List<List<Equipment>> candidatesPerSlot,
                                     Set<UUID> usedEquipmentIds, List<Equipment> result, int depth) {
        if (depth == indexedSlots.size()) {
            return true;
        }

        int slotIndex = indexedSlots.get(depth).originalIndex();
        List<Equipment> candidates = candidatesPerSlot.get(slotIndex);

        long availableCandidates = candidates.stream()
                .filter(e -> !usedEquipmentIds.contains(e.id()))
                .count();

        if (availableCandidates < 1) {
            return false;
        }

        for (Equipment candidate : candidates) {
            if (usedEquipmentIds.contains(candidate.id())) {
                continue;
            }

            usedEquipmentIds.add(candidate.id());
            result.add(candidate);

            if (backtrack(indexedSlots, candidatesPerSlot, usedEquipmentIds, result, depth + 1)) {
                return true;
            }

            usedEquipmentIds.remove(candidate.id());
            result.removeLast();
        }

        return false;
    }

    private record Slot(PolicyItem policyItem) {
    }

    private record IndexedSlot(int originalIndex, int candidateCount) {
    }
}
