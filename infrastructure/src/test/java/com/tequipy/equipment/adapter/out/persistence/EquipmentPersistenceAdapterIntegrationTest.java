package com.tequipy.equipment.adapter.out.persistence;

import com.tequipy.equipment.adapter.out.persistence.mapper.EquipmentPersistenceMapper;
import com.tequipy.equipment.adapter.out.persistence.mapper.EquipmentPersistenceMapperImpl;
import com.tequipy.equipment.adapter.out.persistence.repository.EquipmentJpaRepository;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;
import com.tequipy.equipment.domain.model.EquipmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({EquipmentPersistenceAdapter.class, EquipmentPersistenceMapperImpl.class})
@ActiveProfiles("test")
class EquipmentPersistenceAdapterIntegrationTest {

    @Autowired
    private EquipmentPersistenceAdapter equipmentPersistenceAdapter;

    @Autowired
    private EquipmentJpaRepository equipmentJpaRepository;

    @BeforeEach
    void setUp() {
        equipmentJpaRepository.deleteAll();
    }

    @Test
    void shouldSaveDomainEquipmentAndRetrieveAsdomainObject() {
        // given
        var equipment = createAvailableEquipment(UUID.randomUUID(), "Dell", "Latitude 5540");

        // when
        var savedEquipment = equipmentPersistenceAdapter.save(equipment);

        // then
        assertThat(savedEquipment.id()).isEqualTo(equipment.id());
        assertThat(savedEquipment.type()).isEqualTo(EquipmentType.MAIN_COMPUTER);
        assertThat(savedEquipment.brand()).isEqualTo("Dell");
        assertThat(savedEquipment.model()).isEqualTo("Latitude 5540");
        assertThat(savedEquipment.state()).isEqualTo(EquipmentState.AVAILABLE);
        assertThat(savedEquipment.conditionScore().value()).isEqualByComparingTo("0.9");
        assertThat(savedEquipment.purchaseDate()).isEqualTo(LocalDate.of(2024, 6, 1));
    }

    @Test
    void shouldFindEquipmentByIdAfterSave() {
        // given
        var equipment = createAvailableEquipment(UUID.randomUUID(), "Dell", "Latitude 5540");
        equipmentPersistenceAdapter.save(equipment);

        // when
        var result = equipmentPersistenceAdapter.findById(equipment.id());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(equipment.id());
        assertThat(result.get().brand()).isEqualTo("Dell");
    }

    @Test
    void shouldReturnEmptyOptionalWhenEquipmentNotFound() {
        // given
        var nonexistentId = UUID.randomUUID();

        // when
        var result = equipmentPersistenceAdapter.findById(nonexistentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindEquipmentByState() {
        // given
        equipmentPersistenceAdapter.save(createAvailableEquipment(UUID.randomUUID(), "Dell", "Latitude 5540"));
        equipmentPersistenceAdapter.save(createAvailableEquipment(UUID.randomUUID(), "HP", "EliteBook 840"));

        var retiredEquipment = createAvailableEquipment(UUID.randomUUID(), "Lenovo", "ThinkPad T14");
        retiredEquipment.retire("End of life");
        equipmentPersistenceAdapter.save(retiredEquipment);

        // when
        var availableEquipment = equipmentPersistenceAdapter.findByState(EquipmentState.AVAILABLE);
        var retiredList = equipmentPersistenceAdapter.findByState(EquipmentState.RETIRED);

        // then
        assertThat(availableEquipment).hasSize(2);
        assertThat(availableEquipment).allSatisfy(e -> assertThat(e.state()).isEqualTo(EquipmentState.AVAILABLE));
        assertThat(retiredList).hasSize(1);
        assertThat(retiredList.getFirst().brand()).isEqualTo("Lenovo");
    }

    @Test
    void shouldFindAllAvailableEquipment() {
        // given
        equipmentPersistenceAdapter.save(createAvailableEquipment(UUID.randomUUID(), "Dell", "Latitude 5540"));
        equipmentPersistenceAdapter.save(createAvailableEquipment(UUID.randomUUID(), "HP", "EliteBook 840"));

        var retiredEquipment = createAvailableEquipment(UUID.randomUUID(), "Lenovo", "ThinkPad T14");
        retiredEquipment.retire("Damaged");
        equipmentPersistenceAdapter.save(retiredEquipment);

        // when
        var available = equipmentPersistenceAdapter.findAllAvailable();

        // then
        assertThat(available).hasSize(2);
        assertThat(available).extracting(Equipment::state).containsOnly(EquipmentState.AVAILABLE);
    }

    @Test
    void shouldFindAllEquipmentRegardlessOfState() {
        // given
        equipmentPersistenceAdapter.save(createAvailableEquipment(UUID.randomUUID(), "Dell", "Latitude 5540"));

        var retiredEquipment = createAvailableEquipment(UUID.randomUUID(), "HP", "EliteBook 840");
        retiredEquipment.retire("Obsolete");
        equipmentPersistenceAdapter.save(retiredEquipment);

        // when
        var allEquipment = equipmentPersistenceAdapter.findAll();

        // then
        assertThat(allEquipment).hasSize(2);
    }

    @Test
    void shouldPreserveDomainStateTransitionsAcrossPersistence() {
        // given
        var equipment = createAvailableEquipment(UUID.randomUUID(), "Dell", "Latitude 5540");
        equipmentPersistenceAdapter.save(equipment);

        // when
        equipment.reserve();
        equipmentPersistenceAdapter.save(equipment);

        // then
        var loaded = equipmentPersistenceAdapter.findById(equipment.id());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().state()).isEqualTo(EquipmentState.RESERVED);
    }

    @Test
    void shouldPersistRetireReasonWithRetiredEquipment() {
        // given
        var equipment = createAvailableEquipment(UUID.randomUUID(), "Dell", "Latitude 5540");
        equipment.retire("Screen cracked beyond repair");
        equipmentPersistenceAdapter.save(equipment);

        // when
        var loaded = equipmentPersistenceAdapter.findById(equipment.id());

        // then
        assertThat(loaded).isPresent();
        assertThat(loaded.get().state()).isEqualTo(EquipmentState.RETIRED);
        assertThat(loaded.get().retireReason()).isEqualTo("Screen cracked beyond repair");
    }

    @Test
    void shouldReturnEmptyListWhenNoEquipmentMatchesState() {
        // given
        equipmentPersistenceAdapter.save(createAvailableEquipment(UUID.randomUUID(), "Dell", "Latitude 5540"));

        // when
        var result = equipmentPersistenceAdapter.findByState(EquipmentState.RETIRED);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldMapAllEquipmentTypesCorrectlyThroughPersistence() {
        // given
        for (EquipmentType type : EquipmentType.values()) {
            var equipment = new Equipment(
                    UUID.randomUUID(), type, "TestBrand", "TestModel",
                    EquipmentState.AVAILABLE, ConditionScore.of(0.8),
                    LocalDate.of(2024, 1, 1), null
            );
            equipmentPersistenceAdapter.save(equipment);
        }

        // when
        var allEquipment = equipmentPersistenceAdapter.findAll();

        // then
        assertThat(allEquipment).hasSize(EquipmentType.values().length);
        assertThat(allEquipment).extracting(Equipment::type).containsExactlyInAnyOrder(EquipmentType.values());
    }

    @Test
    void shouldUpdateExistingEquipmentWhenSavedWithSameId() {
        // given
        var equipmentId = UUID.randomUUID();
        var original = createAvailableEquipment(equipmentId, "Dell", "Latitude 5540");
        equipmentPersistenceAdapter.save(original);

        // when
        original.retire("Replaced with newer model");
        equipmentPersistenceAdapter.save(original);

        // then
        var loaded = equipmentPersistenceAdapter.findById(equipmentId);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().state()).isEqualTo(EquipmentState.RETIRED);
        assertThat(loaded.get().retireReason()).isEqualTo("Replaced with newer model");
        assertThat(equipmentJpaRepository.count()).isEqualTo(1);
    }

    private Equipment createAvailableEquipment(UUID id, String brand, String model) {
        return new Equipment(
                id, EquipmentType.MAIN_COMPUTER, brand, model,
                EquipmentState.AVAILABLE, ConditionScore.of(0.9),
                LocalDate.of(2024, 6, 1), null
        );
    }
}
