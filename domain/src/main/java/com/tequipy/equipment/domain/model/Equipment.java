package com.tequipy.equipment.domain.model;

import com.tequipy.equipment.domain.exception.InvalidEquipmentStateTransitionException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Equipment {

    private final UUID id;
    private final EquipmentType type;
    private final String brand;
    private final String model;
    private EquipmentState state;
    private final ConditionScore conditionScore;
    private final LocalDate purchaseDate;
    private String retireReason;

    public Equipment(UUID id, EquipmentType type, String brand, String model,
                     EquipmentState state, ConditionScore conditionScore,
                     LocalDate purchaseDate, String retireReason) {
        this.id = Objects.requireNonNull(id, "Equipment id is required");
        this.type = Objects.requireNonNull(type, "Equipment type is required");
        this.brand = Objects.requireNonNull(brand, "Brand is required");
        this.model = Objects.requireNonNull(model, "Model is required");
        this.state = Objects.requireNonNull(state, "State is required");
        this.conditionScore = Objects.requireNonNull(conditionScore, "Condition score is required");
        this.purchaseDate = Objects.requireNonNull(purchaseDate, "Purchase date is required");
        this.retireReason = retireReason;
    }

    public void reserve() {
        if (state != EquipmentState.AVAILABLE) {
            throw new InvalidEquipmentStateTransitionException(id, state, EquipmentState.RESERVED);
        }
        this.state = EquipmentState.RESERVED;
    }

    public void assign() {
        if (state != EquipmentState.RESERVED) {
            throw new InvalidEquipmentStateTransitionException(id, state, EquipmentState.ASSIGNED);
        }
        this.state = EquipmentState.ASSIGNED;
    }

    public void release() {
        if (state != EquipmentState.RESERVED && state != EquipmentState.ASSIGNED) {
            throw new InvalidEquipmentStateTransitionException(id, state, EquipmentState.AVAILABLE);
        }
        this.state = EquipmentState.AVAILABLE;
    }

    public void retire(String reason) {
        if (state == EquipmentState.RETIRED) {
            throw new InvalidEquipmentStateTransitionException(id, state, EquipmentState.RETIRED);
        }
        Objects.requireNonNull(reason, "Retire reason is required");
        this.state = EquipmentState.RETIRED;
        this.retireReason = reason;
    }

    public UUID id() { return id; }
    public EquipmentType type() { return type; }
    public String brand() { return brand; }
    public String model() { return model; }
    public EquipmentState state() { return state; }
    public ConditionScore conditionScore() { return conditionScore; }
    public LocalDate purchaseDate() { return purchaseDate; }
    public String retireReason() { return retireReason; }
}
