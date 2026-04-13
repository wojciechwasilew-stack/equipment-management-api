package com.tequipy.equipment.adapter.out.persistence.repository;

import com.tequipy.equipment.adapter.out.persistence.entity.AllocationRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AllocationRequestJpaRepository extends JpaRepository<AllocationRequestJpaEntity, UUID> {
}
