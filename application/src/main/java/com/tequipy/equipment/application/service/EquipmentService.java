package com.tequipy.equipment.application.service;

import com.tequipy.equipment.application.port.in.ListEquipmentUseCase;
import com.tequipy.equipment.application.port.in.RegisterEquipmentCommand;
import com.tequipy.equipment.application.port.in.RegisterEquipmentUseCase;
import com.tequipy.equipment.application.port.in.RetireEquipmentUseCase;
import com.tequipy.equipment.application.port.out.LoadEquipmentPort;
import com.tequipy.equipment.application.port.out.SaveEquipmentPort;
import com.tequipy.equipment.domain.exception.EquipmentNotFoundException;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class EquipmentService implements RegisterEquipmentUseCase, ListEquipmentUseCase, RetireEquipmentUseCase {

    private final SaveEquipmentPort saveEquipmentPort;
    private final LoadEquipmentPort loadEquipmentPort;

    @Override
    public UUID register(RegisterEquipmentCommand command) {
        var equipmentId = UUID.randomUUID();
        var equipment = new Equipment(
                equipmentId,
                command.type(),
                command.brand(),
                command.model(),
                EquipmentState.AVAILABLE,
                ConditionScore.of(command.conditionScore()),
                command.purchaseDate(),
                null
        );
        saveEquipmentPort.save(equipment);
        log.info("Registered equipment with id {}", equipmentId);
        return equipmentId;
    }

    @Override
    public List<Equipment> listByState(EquipmentState state) {
        if (state == null) {
            return loadEquipmentPort.findAll();
        }
        return loadEquipmentPort.findByState(state);
    }

    @Override
    public List<Equipment> listAll() {
        return loadEquipmentPort.findAll();
    }

    @Override
    public void retire(UUID equipmentId, String reason) {
        var equipment = loadEquipmentPort.findById(equipmentId)
                .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));
        equipment.retire(reason);
        saveEquipmentPort.save(equipment);
        log.info("Retired equipment {} with reason: {}", equipmentId, reason);
    }
}
