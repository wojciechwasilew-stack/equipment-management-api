package com.tequipy.equipment.adapter.out.persistence;

import com.tequipy.equipment.adapter.out.persistence.mapper.EquipmentPersistenceMapper;
import com.tequipy.equipment.adapter.out.persistence.repository.EquipmentJpaRepository;
import com.tequipy.equipment.application.port.out.LoadEquipmentPort;
import com.tequipy.equipment.application.port.out.SaveEquipmentPort;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EquipmentPersistenceAdapter implements SaveEquipmentPort, LoadEquipmentPort {

    private final EquipmentJpaRepository equipmentJpaRepository;
    private final EquipmentPersistenceMapper equipmentPersistenceMapper;

    @Override
    @CacheEvict(value = "equipment", allEntries = true)
    public Equipment save(Equipment equipment) {
        var entity = equipmentPersistenceMapper.toJpaEntity(equipment);
        var savedEntity = equipmentJpaRepository.save(entity);
        return equipmentPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Equipment> findById(UUID id) {
        return equipmentJpaRepository.findById(id)
                .map(equipmentPersistenceMapper::toDomain);
    }

    @Override
    @Cacheable(value = "equipment", key = "'state:' + #state.name()")
    public List<Equipment> findByState(EquipmentState state) {
        return equipmentJpaRepository.findByState(state.name()).stream()
                .map(equipmentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Equipment> findAllAvailable() {
        return findByState(EquipmentState.AVAILABLE);
    }

    @Override
    @Cacheable(value = "equipment", key = "'all'")
    public List<Equipment> findAll() {
        return equipmentJpaRepository.findAll().stream()
                .map(equipmentPersistenceMapper::toDomain)
                .toList();
    }
}
