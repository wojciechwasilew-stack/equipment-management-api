package com.tequipy.equipment.application.service;

import com.tequipy.equipment.application.port.out.LoadAllocationPort;
import com.tequipy.equipment.application.port.out.LoadEquipmentPort;
import com.tequipy.equipment.application.port.out.SaveAllocationPort;
import com.tequipy.equipment.application.port.out.SaveEquipmentPort;
import com.tequipy.equipment.domain.exception.AllocationNotFoundException;
import com.tequipy.equipment.domain.model.AllocationRequest;
import com.tequipy.equipment.domain.model.AllocationState;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;
import com.tequipy.equipment.domain.model.EquipmentType;
import com.tequipy.equipment.domain.model.PolicyItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AllocationProcessorServiceTest {

    @Mock
    private LoadAllocationPort loadAllocationPort;

    @Mock
    private SaveAllocationPort saveAllocationPort;

    @Mock
    private LoadEquipmentPort loadEquipmentPort;

    @Mock
    private SaveEquipmentPort saveEquipmentPort;

    @InjectMocks
    private AllocationProcessorService allocationProcessorService;

    @Test
    void shouldProcessAllocationSuccessfully() {
        // given
        var allocationId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId);
        var laptop = createEquipment(EquipmentType.MAIN_COMPUTER, 0.9);

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findAllAvailable()).willReturn(List.of(laptop));
        given(saveEquipmentPort.save(any(Equipment.class))).willAnswer(inv -> inv.getArgument(0));
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationProcessorService.process(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.ALLOCATED);
        assertThat(allocation.allocatedEquipmentIds()).hasSize(1);
        assertThat(laptop.state()).isEqualTo(EquipmentState.RESERVED);
        then(saveEquipmentPort).should().save(laptop);
        then(saveAllocationPort).should().save(allocation);
    }

    @Test
    void shouldMarkAllocationFailedWhenNoSuitableEquipment() {
        // given
        var allocationId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId);

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findAllAvailable()).willReturn(List.of());
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationProcessorService.process(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.FAILED);
        then(saveAllocationPort).should().save(allocation);
        then(saveEquipmentPort).should(times(0)).save(any(Equipment.class));
    }

    @Test
    void shouldThrowWhenAllocationRequestNotFound() {
        // given
        var allocationId = UUID.randomUUID();
        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> allocationProcessorService.process(allocationId))
                .isInstanceOf(AllocationNotFoundException.class);
    }

    private AllocationRequest createAllocationRequest(UUID id) {
        var policyItem = new PolicyItem(
                EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false
        );
        return new AllocationRequest(id, "EMP-001", List.of(policyItem), AllocationState.PENDING, List.of());
    }

    private Equipment createEquipment(EquipmentType type, double conditionScore) {
        return new Equipment(
                UUID.randomUUID(),
                type,
                "Dell",
                "Model-X",
                EquipmentState.AVAILABLE,
                ConditionScore.of(conditionScore),
                LocalDate.of(2024, 1, 15),
                null
        );
    }
}
