package com.tequipy.equipment.adapter.out.persistence;

import com.tequipy.equipment.adapter.out.persistence.entity.EquipmentJpaEntity;
import com.tequipy.equipment.adapter.out.persistence.repository.EquipmentJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EquipmentRepositoryIntegrationTest {

    @Autowired
    private EquipmentJpaRepository equipmentJpaRepository;

    @Test
    void shouldPersistAndRetrieveEquipmentById() {
        // given
        var entity = createEquipmentEntity(UUID.randomUUID(), "AVAILABLE");
        equipmentJpaRepository.save(entity);

        // when
        var result = equipmentJpaRepository.findById(entity.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getBrand()).isEqualTo("Dell");
        assertThat(result.get().getModel()).isEqualTo("Latitude 5540");
        assertThat(result.get().getState()).isEqualTo("AVAILABLE");
    }

    @Test
    void shouldReturnEmptyWhenEquipmentNotFound() {
        // given
        var nonexistentId = UUID.randomUUID();

        // when
        var result = equipmentJpaRepository.findById(nonexistentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindEquipmentByState() {
        // given
        equipmentJpaRepository.save(createEquipmentEntity(UUID.randomUUID(), "AVAILABLE"));
        equipmentJpaRepository.save(createEquipmentEntity(UUID.randomUUID(), "AVAILABLE"));
        equipmentJpaRepository.save(createEquipmentEntity(UUID.randomUUID(), "RETIRED"));

        // when
        var availableEquipment = equipmentJpaRepository.findByState("AVAILABLE");
        var retiredEquipment = equipmentJpaRepository.findByState("RETIRED");

        // then
        assertThat(availableEquipment).hasSize(2);
        assertThat(retiredEquipment).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenNoEquipmentMatchesState() {
        // given
        equipmentJpaRepository.save(createEquipmentEntity(UUID.randomUUID(), "AVAILABLE"));

        // when
        var result = equipmentJpaRepository.findByState("RETIRED");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateEquipmentState() {
        // given
        var entity = createEquipmentEntity(UUID.randomUUID(), "AVAILABLE");
        equipmentJpaRepository.save(entity);

        // when
        entity.setState("RETIRED");
        entity.setRetireReason("End of life");
        equipmentJpaRepository.save(entity);

        // then
        var updated = equipmentJpaRepository.findById(entity.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getState()).isEqualTo("RETIRED");
        assertThat(updated.get().getRetireReason()).isEqualTo("End of life");
    }

    @Test
    void shouldDeleteEquipment() {
        // given
        var entity = createEquipmentEntity(UUID.randomUUID(), "AVAILABLE");
        equipmentJpaRepository.save(entity);

        // when
        equipmentJpaRepository.deleteById(entity.getId());

        // then
        assertThat(equipmentJpaRepository.findById(entity.getId())).isEmpty();
    }

    private EquipmentJpaEntity createEquipmentEntity(UUID id, String state) {
        return new EquipmentJpaEntity(id, "MAIN_COMPUTER", "Dell", "Latitude 5540",
                state, BigDecimal.valueOf(0.90), LocalDate.of(2024, 6, 1), null);
    }
}
