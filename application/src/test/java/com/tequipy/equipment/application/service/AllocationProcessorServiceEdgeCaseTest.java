package com.tequipy.equipment.application.service;

import com.tequipy.equipment.application.port.out.LoadAllocationPort;
import com.tequipy.equipment.application.port.out.LoadEquipmentPort;
import com.tequipy.equipment.application.port.out.SaveAllocationPort;
import com.tequipy.equipment.application.port.out.SaveEquipmentPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AllocationProcessorServiceEdgeCaseTest {

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
    void shouldProcessAllocationWithMultipleEquipmentItems() {
        // given
        var allocationId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId,
                List.of(new PolicyItem(EquipmentType.MONITOR, 2, ConditionScore.of(0.5), null, false)));
        var monitorA = createEquipment(EquipmentType.MONITOR, 0.9);
        var monitorB = createEquipment(EquipmentType.MONITOR, 0.8);

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findAllAvailable()).willReturn(List.of(monitorA, monitorB));
        given(saveEquipmentPort.save(any(Equipment.class))).willAnswer(inv -> inv.getArgument(0));
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationProcessorService.process(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.ALLOCATED);
        assertThat(allocation.allocatedEquipmentIds()).hasSize(2);
        assertThat(monitorA.state()).isEqualTo(EquipmentState.RESERVED);
        assertThat(monitorB.state()).isEqualTo(EquipmentState.RESERVED);
        then(saveEquipmentPort).should(times(2)).save(any(Equipment.class));
    }

    @Test
    void shouldMarkFailedWhenConditionScoreTooLow() {
        // given
        var allocationId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId,
                List.of(new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.9), null, false)));
        var lowConditionLaptop = createEquipment(EquipmentType.MAIN_COMPUTER, 0.3);

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findAllAvailable()).willReturn(List.of(lowConditionLaptop));
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationProcessorService.process(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.FAILED);
        then(saveEquipmentPort).should(times(0)).save(any(Equipment.class));
    }

    @Test
    void shouldMarkFailedWhenEquipmentTypeMismatch() {
        // given
        var allocationId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId,
                List.of(new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false)));
        var monitor = createEquipment(EquipmentType.MONITOR, 0.9);

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findAllAvailable()).willReturn(List.of(monitor));
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationProcessorService.process(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.FAILED);
    }

    @Test
    void shouldProcessMultiplePolicyItemTypes() {
        // given
        var allocationId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId, List.of(
                new PolicyItem(EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false),
                new PolicyItem(EquipmentType.MONITOR, 1, ConditionScore.of(0.5), null, false)
        ));
        var laptop = createEquipment(EquipmentType.MAIN_COMPUTER, 0.9);
        var monitor = createEquipment(EquipmentType.MONITOR, 0.8);

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findAllAvailable()).willReturn(List.of(laptop, monitor));
        given(saveEquipmentPort.save(any(Equipment.class))).willAnswer(inv -> inv.getArgument(0));
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationProcessorService.process(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.ALLOCATED);
        assertThat(allocation.allocatedEquipmentIds()).hasSize(2);
        then(saveEquipmentPort).should(times(2)).save(any(Equipment.class));
    }

    @Test
    void shouldMarkFailedWhenNotEnoughQuantityAvailable() {
        // given
        var allocationId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId,
                List.of(new PolicyItem(EquipmentType.MONITOR, 3, ConditionScore.of(0.5), null, false)));
        var monitor = createEquipment(EquipmentType.MONITOR, 0.9);

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findAllAvailable()).willReturn(List.of(monitor));
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationProcessorService.process(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.FAILED);
    }

    private AllocationRequest createAllocationRequest(UUID id, List<PolicyItem> policyItems) {
        return new AllocationRequest(id, "EMP-001", policyItems, AllocationState.PENDING, List.of());
    }

    private Equipment createEquipment(EquipmentType type, double conditionScore) {
        return new Equipment(
                UUID.randomUUID(), type, "Dell", "Model-X",
                EquipmentState.AVAILABLE, ConditionScore.of(conditionScore),
                LocalDate.of(2024, 1, 15), null
        );
    }
}
