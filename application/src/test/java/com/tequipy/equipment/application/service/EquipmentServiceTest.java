package com.tequipy.equipment.application.service;

import com.tequipy.equipment.application.port.in.RegisterEquipmentCommand;
import com.tequipy.equipment.application.port.out.LoadEquipmentPort;
import com.tequipy.equipment.application.port.out.SaveEquipmentPort;
import com.tequipy.equipment.domain.exception.EquipmentNotFoundException;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;
import com.tequipy.equipment.domain.model.EquipmentType;
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

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private SaveEquipmentPort saveEquipmentPort;

    @Mock
    private LoadEquipmentPort loadEquipmentPort;

    @InjectMocks
    private EquipmentService equipmentService;

    @Test
    void shouldRegisterEquipmentSuccessfully() {
        // given
        var command = new RegisterEquipmentCommand(
                EquipmentType.MAIN_COMPUTER,
                "Dell",
                "Latitude 5540",
                BigDecimal.valueOf(0.9),
                LocalDate.of(2024, 6, 1)
        );
        given(saveEquipmentPort.save(any(Equipment.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        var result = equipmentService.register(command);

        // then
        assertThat(result).isNotNull();
        var captor = ArgumentCaptor.forClass(Equipment.class);
        then(saveEquipmentPort).should().save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.type()).isEqualTo(EquipmentType.MAIN_COMPUTER);
        assertThat(saved.brand()).isEqualTo("Dell");
        assertThat(saved.model()).isEqualTo("Latitude 5540");
        assertThat(saved.state()).isEqualTo(EquipmentState.AVAILABLE);
    }

    @Test
    void shouldListByStateWhenStateProvided() {
        // given
        var equipment = createEquipment(EquipmentState.AVAILABLE);
        given(loadEquipmentPort.findByState(EquipmentState.AVAILABLE)).willReturn(List.of(equipment));

        // when
        var result = equipmentService.listByState(EquipmentState.AVAILABLE);

        // then
        assertThat(result).hasSize(1);
        then(loadEquipmentPort).should().findByState(EquipmentState.AVAILABLE);
    }

    @Test
    void shouldListAllWhenStateNull() {
        // given
        var equipment = createEquipment(EquipmentState.AVAILABLE);
        given(loadEquipmentPort.findAll()).willReturn(List.of(equipment));

        // when
        var result = equipmentService.listByState(null);

        // then
        assertThat(result).hasSize(1);
        then(loadEquipmentPort).should().findAll();
    }

    @Test
    void shouldRetireEquipmentSuccessfully() {
        // given
        var equipmentId = UUID.randomUUID();
        var equipment = createEquipmentWithId(equipmentId, EquipmentState.AVAILABLE);
        given(loadEquipmentPort.findById(equipmentId)).willReturn(Optional.of(equipment));
        given(saveEquipmentPort.save(any(Equipment.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        equipmentService.retire(equipmentId, "End of life");

        // then
        assertThat(equipment.state()).isEqualTo(EquipmentState.RETIRED);
        assertThat(equipment.retireReason()).isEqualTo("End of life");
        then(saveEquipmentPort).should().save(equipment);
    }

    @Test
    void shouldThrowWhenRetiringNonexistentEquipment() {
        // given
        var equipmentId = UUID.randomUUID();
        given(loadEquipmentPort.findById(equipmentId)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> equipmentService.retire(equipmentId, "End of life"))
                .isInstanceOf(EquipmentNotFoundException.class);
    }

    private Equipment createEquipment(EquipmentState state) {
        return createEquipmentWithId(UUID.randomUUID(), state);
    }

    private Equipment createEquipmentWithId(UUID id, EquipmentState state) {
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
