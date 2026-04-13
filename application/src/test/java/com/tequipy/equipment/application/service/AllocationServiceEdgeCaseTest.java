package com.tequipy.equipment.application.service;

import com.tequipy.equipment.application.port.in.CreateAllocationCommand;
import com.tequipy.equipment.application.port.out.LoadAllocationPort;
import com.tequipy.equipment.application.port.out.LoadEquipmentPort;
import com.tequipy.equipment.application.port.out.PublishAllocationEventPort;
import com.tequipy.equipment.application.port.out.SaveAllocationPort;
import com.tequipy.equipment.application.port.out.SaveEquipmentPort;
import com.tequipy.equipment.domain.event.AllocationCreatedEvent;
import com.tequipy.equipment.domain.exception.AllocationNotFoundException;
import com.tequipy.equipment.domain.exception.EquipmentNotFoundException;
import com.tequipy.equipment.domain.model.AllocationRequest;
import com.tequipy.equipment.domain.model.AllocationState;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;
import com.tequipy.equipment.domain.model.EquipmentType;
import com.tequipy.equipment.domain.model.PolicyItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AllocationServiceEdgeCaseTest {

    @Mock
    private SaveAllocationPort saveAllocationPort;

    @Mock
    private LoadAllocationPort loadAllocationPort;

    @Mock
    private SaveEquipmentPort saveEquipmentPort;

    @Mock
    private LoadEquipmentPort loadEquipmentPort;

    @Mock
    private PublishAllocationEventPort publishAllocationEventPort;

    @InjectMocks
    private AllocationService allocationService;

    @Test
    void shouldThrowWhenConfirmingNonExistentAllocation() {
        // given
        var allocationId = UUID.randomUUID();
        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> allocationService.confirm(allocationId))
                .isInstanceOf(AllocationNotFoundException.class);
    }

    @Test
    void shouldThrowWhenCancellingNonExistentAllocation() {
        // given
        var allocationId = UUID.randomUUID();
        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> allocationService.cancel(allocationId))
                .isInstanceOf(AllocationNotFoundException.class);
    }

    @Test
    void shouldCancelPendingAllocationWithoutReleasingEquipment() {
        // given
        var allocationId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId, AllocationState.PENDING, List.of());
        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationService.cancel(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.CANCELLED);
        then(loadEquipmentPort).should(never()).findById(any());
        then(saveEquipmentPort).should(never()).save(any());
    }

    @Test
    void shouldConfirmMultipleEquipmentItems() {
        // given
        var allocationId = UUID.randomUUID();
        var equipmentIdA = UUID.randomUUID();
        var equipmentIdB = UUID.randomUUID();
        var equipmentIdC = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId, AllocationState.ALLOCATED,
                List.of(equipmentIdA, equipmentIdB, equipmentIdC));
        var equipmentA = createEquipment(equipmentIdA, EquipmentState.RESERVED);
        var equipmentB = createEquipment(equipmentIdB, EquipmentState.RESERVED);
        var equipmentC = createEquipment(equipmentIdC, EquipmentState.RESERVED);

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findById(equipmentIdA)).willReturn(Optional.of(equipmentA));
        given(loadEquipmentPort.findById(equipmentIdB)).willReturn(Optional.of(equipmentB));
        given(loadEquipmentPort.findById(equipmentIdC)).willReturn(Optional.of(equipmentC));
        given(saveEquipmentPort.save(any(Equipment.class))).willAnswer(inv -> inv.getArgument(0));
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationService.confirm(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.CONFIRMED);
        assertThat(equipmentA.state()).isEqualTo(EquipmentState.ASSIGNED);
        assertThat(equipmentB.state()).isEqualTo(EquipmentState.ASSIGNED);
        assertThat(equipmentC.state()).isEqualTo(EquipmentState.ASSIGNED);
        then(saveEquipmentPort).should(times(3)).save(any(Equipment.class));
    }

    @Test
    void shouldCancelMultipleEquipmentItems() {
        // given
        var allocationId = UUID.randomUUID();
        var equipmentIdA = UUID.randomUUID();
        var equipmentIdB = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId, AllocationState.ALLOCATED,
                List.of(equipmentIdA, equipmentIdB));
        var equipmentA = createEquipment(equipmentIdA, EquipmentState.RESERVED);
        var equipmentB = createEquipment(equipmentIdB, EquipmentState.RESERVED);

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findById(equipmentIdA)).willReturn(Optional.of(equipmentA));
        given(loadEquipmentPort.findById(equipmentIdB)).willReturn(Optional.of(equipmentB));
        given(saveEquipmentPort.save(any(Equipment.class))).willAnswer(inv -> inv.getArgument(0));
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationService.cancel(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.CANCELLED);
        assertThat(equipmentA.state()).isEqualTo(EquipmentState.AVAILABLE);
        assertThat(equipmentB.state()).isEqualTo(EquipmentState.AVAILABLE);
        then(saveEquipmentPort).should(times(2)).save(any(Equipment.class));
    }

    @Test
    void shouldThrowWhenConfirmingAndEquipmentNotFound() {
        // given
        var allocationId = UUID.randomUUID();
        var missingEquipmentId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId, AllocationState.ALLOCATED, List.of(missingEquipmentId));

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findById(missingEquipmentId)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> allocationService.confirm(allocationId))
                .isInstanceOf(EquipmentNotFoundException.class);
    }

    @Test
    void shouldThrowWhenCancellingAndEquipmentNotFound() {
        // given
        var allocationId = UUID.randomUUID();
        var missingEquipmentId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId, AllocationState.ALLOCATED, List.of(missingEquipmentId));

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findById(missingEquipmentId)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> allocationService.cancel(allocationId))
                .isInstanceOf(EquipmentNotFoundException.class);
    }

    @Test
    void shouldCreateAllocationWithMultiplePolicyItems() {
        // given
        var command = new CreateAllocationCommand(
                "EMP-002",
                List.of(
                        new CreateAllocationCommand.PolicyItemCommand(
                                EquipmentType.MAIN_COMPUTER, 1, BigDecimal.valueOf(0.8), "Dell", true
                        ),
                        new CreateAllocationCommand.PolicyItemCommand(
                                EquipmentType.MONITOR, 2, BigDecimal.valueOf(0.5), null, false
                        )
                )
        );
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        var result = allocationService.create(command);

        // then
        assertThat(result).isNotNull();
        var captor = ArgumentCaptor.forClass(AllocationRequest.class);
        then(saveAllocationPort).should().save(captor.capture());
        var savedAllocation = captor.getValue();
        assertThat(savedAllocation.policyItems()).hasSize(2);
        assertThat(savedAllocation.state()).isEqualTo(AllocationState.PENDING);
        assertThat(savedAllocation.employeeId()).isEqualTo("EMP-002");
        then(publishAllocationEventPort).should().publish(any(AllocationCreatedEvent.class));
    }

    @Test
    void shouldMapPolicyItemCommandCorrectlyDuringCreation() {
        // given
        var command = new CreateAllocationCommand(
                "EMP-003",
                List.of(new CreateAllocationCommand.PolicyItemCommand(
                        EquipmentType.KEYBOARD, 3, BigDecimal.valueOf(0.6), "Logitech", true
                ))
        );
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationService.create(command);

        // then
        var captor = ArgumentCaptor.forClass(AllocationRequest.class);
        then(saveAllocationPort).should().save(captor.capture());
        var policyItem = captor.getValue().policyItems().getFirst();
        assertThat(policyItem.equipmentType()).isEqualTo(EquipmentType.KEYBOARD);
        assertThat(policyItem.quantity()).isEqualTo(3);
        assertThat(policyItem.minimumConditionScore()).isEqualTo(ConditionScore.of(0.6));
        assertThat(policyItem.preferredBrand()).isEqualTo("Logitech");
        assertThat(policyItem.preferRecent()).isTrue();
    }

    private AllocationRequest createAllocationRequest(UUID id, AllocationState state, List<UUID> equipmentIds) {
        var policyItem = new PolicyItem(
                EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false
        );
        return new AllocationRequest(id, "EMP-001", List.of(policyItem), state, equipmentIds);
    }

    private Equipment createEquipment(UUID id, EquipmentState state) {
        return new Equipment(
                id, EquipmentType.MAIN_COMPUTER, "Dell", "Latitude 5540",
                state, ConditionScore.of(0.9), LocalDate.of(2024, 1, 15), null
        );
    }
}
