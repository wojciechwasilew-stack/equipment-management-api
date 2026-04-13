package com.tequipy.equipment.adapter.out.persistence.mapper;

import com.tequipy.equipment.adapter.out.persistence.entity.EquipmentJpaEntity;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;
import com.tequipy.equipment.domain.model.EquipmentType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EquipmentPersistenceMapper {

    default EquipmentJpaEntity toJpaEntity(Equipment equipment) {
        var entity = new EquipmentJpaEntity();
        entity.setId(equipment.id());
        entity.setType(equipment.type().name());
        entity.setBrand(equipment.brand());
        entity.setModel(equipment.model());
        entity.setState(equipment.state().name());
        entity.setConditionScore(equipment.conditionScore().value());
        entity.setPurchaseDate(equipment.purchaseDate());
        entity.setRetireReason(equipment.retireReason());
        return entity;
    }

    default Equipment toDomain(EquipmentJpaEntity entity) {
        return new Equipment(
                entity.getId(),
                EquipmentType.valueOf(entity.getType()),
                entity.getBrand(),
                entity.getModel(),
                EquipmentState.valueOf(entity.getState()),
                ConditionScore.of(entity.getConditionScore()),
                entity.getPurchaseDate(),
                entity.getRetireReason()
        );
    }
}
