package com.tequipy.equipment.adapter.out.persistence.repository;

import com.tequipy.equipment.adapter.out.persistence.entity.EquipmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EquipmentJpaRepository extends JpaRepository<EquipmentJpaEntity, UUID> {

    List<EquipmentJpaEntity> findByState(String state);
}
