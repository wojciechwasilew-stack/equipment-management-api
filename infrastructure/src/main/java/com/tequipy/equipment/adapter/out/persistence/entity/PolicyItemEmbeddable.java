package com.tequipy.equipment.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyItemEmbeddable {

    @Column(name = "equipment_type", nullable = false, length = 50)
    private String equipmentType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "minimum_condition_score", precision = 3, scale = 2)
    private BigDecimal minimumConditionScore;

    @Column(name = "preferred_brand")
    private String preferredBrand;

    @Column(name = "prefer_recent", nullable = false)
    private boolean preferRecent;
}
