package com.tequipy.equipment.application.port.in;

import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;

import java.util.List;

public interface ListEquipmentUseCase {

    List<Equipment> listByState(EquipmentState state);

    List<Equipment> listAll();
}
