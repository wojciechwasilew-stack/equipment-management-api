package com.tequipy.equipment.adapter.in.rest.mapper;

import com.tequipy.equipment.adapter.in.rest.dto.EquipmentResponse;
import com.tequipy.equipment.adapter.in.rest.dto.RegisterEquipmentRequest;
import com.tequipy.equipment.application.port.in.RegisterEquipmentCommand;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentType;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EquipmentRestMapper {

    default RegisterEquipmentCommand toCommand(RegisterEquipmentRequest request) {
        return new RegisterEquipmentCommand(
                EquipmentType.valueOf(request.type()),
                request.brand(),
                request.model(),
                request.conditionScore(),
                request.purchaseDate()
        );
    }

    default EquipmentResponse toResponse(Equipment equipment) {
        return new EquipmentResponse(
                equipment.id(),
                equipment.type().name(),
                equipment.brand(),
                equipment.model(),
                equipment.state().name(),
                equipment.conditionScore().value(),
                equipment.purchaseDate(),
                equipment.retireReason()
        );
    }

    default List<EquipmentResponse> toResponseList(List<Equipment> equipmentList) {
        return equipmentList.stream()
                .map(this::toResponse)
                .toList();
    }
}
