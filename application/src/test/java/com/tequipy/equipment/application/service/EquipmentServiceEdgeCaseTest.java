package com.tequipy.equipment.application.service;

import com.tequipy.equipment.application.port.in.RegisterEquipmentCommand;
import com.tequipy.equipment.application.port.out.LoadEquipmentPort;
import com.tequipy.equipment.application.port.out.SaveEquipmentPort;
import com.tequipy.equipment.domain.exception.EquipmentNotFoundException;
import com.tequipy.equipment.domain.exception.InvalidEquipmentStateTransitionException;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;
import com.tequipy.equipment.domain.model.EquipmentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

@ExtendWith(MockitoExtension.class)
class EquipmentServiceEdgeCaseTest {

    @Mock
    private SaveEquipmentPort saveEquipmentPort;

    @Mock
    private LoadEquipmentPort loadEquipmentPort;

    @InjectMocks
    private EquipmentService equipmentService;

    @Test
    void shouldReturnAllEquipmentWhenListAllCalled() {
        // given
        var equipmentA = createEquipment(EquipmentState.AVAILABLE);
        var equipmentB = createEquipment(EquipmentState.RETIRED);
        given(loadEquipmentPort.findAll()).willReturn(List.of(equipmentA, equipmentB));

        // when
        var result = equipmentService.listAll();

        // then
        assertThat(result).hasSize(2);
        then(loadEquipmentPort).should().findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoEquipmentExists() {
        // given
        given(loadEquipmentPort.findAll()).willReturn(List.of());

        // when
        var result = equipmentService.listAll();

        // then
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(EquipmentState.class)
    void shouldDelegateToFindByStateForEachState(EquipmentState state) {
        // given
        given(loadEquipmentPort.findByState(state)).willReturn(List.of());

        // when
        var result = equipmentService.listByState(state);

        // then
        assertThat(result).isEmpty();
        then(loadEquipmentPort).should().findByState(state);
    }

    @Test
    void shouldSetConditionScoreFromCommandWhenRegistering() {
        // given
        var command = new RegisterEquipmentCommand(
                EquipmentType.MONITOR,
                "LG",
                "27UK850",
                BigDecimal.valueOf(0.75),
                LocalDate.of(2024, 3, 15)
        );
        given(saveEquipmentPort.save(any(Equipment.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        equipmentService.register(command);

        // then
        var captor = ArgumentCaptor.forClass(Equipment.class);
        then(saveEquipmentPort).should().save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.conditionScore()).isEqualTo(ConditionScore.of(0.75));
        assertThat(saved.purchaseDate()).isEqualTo(LocalDate.of(2024, 3, 15));
    }

    @Test
    void shouldThrowWhenRetiringAlreadyRetiredEquipment() {
        // given
        var equipmentId = UUID.randomUUID();
        var retiredEquipment = new Equipment(
                equipmentId, EquipmentType.MAIN_COMPUTER, "Dell", "Latitude 5540",
                EquipmentState.RETIRED, ConditionScore.of(0.9), LocalDate.of(2024, 1, 15), "Old reason"
        );
        given(loadEquipmentPort.findById(equipmentId)).willReturn(Optional.of(retiredEquipment));

        // then
        assertThatThrownBy(() -> equipmentService.retire(equipmentId, "New reason"))
                .isInstanceOf(InvalidEquipmentStateTransitionException.class);
    }

    @Test
    void shouldGenerateUniqueIdForEachRegistration() {
        // given
        var command = new RegisterEquipmentCommand(
                EquipmentType.KEYBOARD, "Logitech", "MX Keys", BigDecimal.valueOf(1.0), LocalDate.now()
        );
        given(saveEquipmentPort.save(any(Equipment.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        var firstId = equipmentService.register(command);
        var secondId = equipmentService.register(command);

        // then
        assertThat(firstId).isNotEqualTo(secondId);
    }

    private Equipment createEquipment(EquipmentState state) {
        return new Equipment(
                UUID.randomUUID(), EquipmentType.MAIN_COMPUTER, "Dell", "Latitude 5540",
                state, ConditionScore.of(0.9), LocalDate.of(2024, 1, 15),
                state == EquipmentState.RETIRED ? "Legacy reason" : null
        );
    }
}
