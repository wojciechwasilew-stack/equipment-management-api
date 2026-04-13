package com.tequipy.equipment.adapter.out.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "allocation_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRequestJpaEntity {

    @Id
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(nullable = false, length = 50)
    private String state;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "allocation_equipment",
            joinColumns = @JoinColumn(name = "allocation_request_id")
    )
    @Column(name = "equipment_id")
    private List<UUID> allocatedEquipmentIds = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "policy_item",
            joinColumns = @JoinColumn(name = "allocation_request_id")
    )
    private List<PolicyItemEmbeddable> policyItems = new ArrayList<>();
}
