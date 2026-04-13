package com.tequipy.equipment.application.service;

import com.tequipy.equipment.application.port.in.CreateAllocationCommand;
import com.tequipy.equipment.application.port.out.LoadAllocationPort;
import com.tequipy.equipment.application.port.out.LoadEquipmentPort;
import com.tequipy.equipment.application.port.out.PublishAllocationEventPort;
import com.tequipy.equipment.application.port.out.SaveAllocationPort;
import com.tequipy.equipment.application.port.out.SaveEquipmentPort;
import com.tequipy.equipment.domain.event.AllocationCreatedEvent;
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

@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {

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
    void shouldCreateAllocationAndPublishEvent() {
        // given
        var command = new CreateAllocationCommand(
                "EMP-001",
                List.of(new CreateAllocationCommand.PolicyItemCommand(
                        EquipmentType.MAIN_COMPUTER, 1, BigDecimal.valueOf(0.5), "Dell", false
                ))
        );
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        var result = allocationService.create(command);

        // then
        assertThat(result).isNotNull();
        then(saveAllocationPort).should().save(any(AllocationRequest.class));
        then(publishAllocationEventPort).should().publish(any(AllocationCreatedEvent.class));
    }

    @Test
    void shouldGetAllocationById() {
        // given
        var allocationId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId, AllocationState.PENDING);
        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));

        // when
        var result = allocationService.getById(allocationId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(allocationId);
    }

    @Test
    void shouldThrowWhenAllocationNotFound() {
        // given
        var allocationId = UUID.randomUUID();
        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> allocationService.getById(allocationId))
                .isInstanceOf(AllocationNotFoundException.class);
    }

    @Test
    void shouldConfirmAllocationAndAssignEquipment() {
        // given
        var allocationId = UUID.randomUUID();
        var equipmentId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId, AllocationState.ALLOCATED, List.of(equipmentId));
        var equipment = createEquipment(equipmentId, EquipmentState.RESERVED);

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findById(equipmentId)).willReturn(Optional.of(equipment));
        given(saveEquipmentPort.save(any(Equipment.class))).willAnswer(inv -> inv.getArgument(0));
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationService.confirm(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.CONFIRMED);
        assertThat(equipment.state()).isEqualTo(EquipmentState.ASSIGNED);
        then(saveEquipmentPort).should().save(equipment);
        then(saveAllocationPort).should().save(allocation);
    }

    @Test
    void shouldCancelAllocationAndReleaseEquipment() {
        // given
        var allocationId = UUID.randomUUID();
        var equipmentId = UUID.randomUUID();
        var allocation = createAllocationRequest(allocationId, AllocationState.ALLOCATED, List.of(equipmentId));
        var equipment = createEquipment(equipmentId, EquipmentState.RESERVED);

        given(loadAllocationPort.findById(allocationId)).willReturn(Optional.of(allocation));
        given(loadEquipmentPort.findById(equipmentId)).willReturn(Optional.of(equipment));
        given(saveEquipmentPort.save(any(Equipment.class))).willAnswer(inv -> inv.getArgument(0));
        given(saveAllocationPort.save(any(AllocationRequest.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        allocationService.cancel(allocationId);

        // then
        assertThat(allocation.state()).isEqualTo(AllocationState.CANCELLED);
        assertThat(equipment.state()).isEqualTo(EquipmentState.AVAILABLE);
        then(saveEquipmentPort).should().save(equipment);
        then(saveAllocationPort).should().save(allocation);
    }

    private AllocationRequest createAllocationRequest(UUID id, AllocationState state) {
        return createAllocationRequest(id, state, List.of());
    }

    private AllocationRequest createAllocationRequest(UUID id, AllocationState state, List<UUID> equipmentIds) {
        var policyItem = new PolicyItem(
                EquipmentType.MAIN_COMPUTER, 1, ConditionScore.of(0.5), null, false
        );
        return new AllocationRequest(id, "EMP-001", List.of(policyItem), state, equipmentIds);
    }

    private Equipment createEquipment(UUID id, EquipmentState state) {
        return new Equipment(
                id,
                EquipmentType.MAIN_COMPUTER,
                "Dell",
                "Latitude 5540",
                state,
                ConditionScore.of(0.9),
                LocalDate.of(2024, 1, 15),
                null
        );
    }
}
