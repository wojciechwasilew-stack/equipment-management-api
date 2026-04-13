package com.tequipy.equipment.application.port.out;

import com.tequipy.equipment.domain.model.Equipment;

public interface SaveEquipmentPort {

    Equipment save(Equipment equipment);
}
