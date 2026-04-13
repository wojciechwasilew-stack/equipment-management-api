package com.tequipy.equipment.application.port.out;

import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadEquipmentPort {

    Optional<Equipment> findById(UUID id);

    List<Equipment> findByState(EquipmentState state);

    List<Equipment> findAllAvailable();

    List<Equipment> findAll();
}
