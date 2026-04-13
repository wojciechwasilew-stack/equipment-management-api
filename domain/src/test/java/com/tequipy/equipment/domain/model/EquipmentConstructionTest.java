package com.tequipy.equipment.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EquipmentConstructionTest {

    private static final UUID EQUIPMENT_ID = UUID.randomUUID();
    private static final EquipmentType DEFAULT_TYPE = EquipmentType.MAIN_COMPUTER;
    private static final String DEFAULT_BRAND = "Dell";
    private static final String DEFAULT_MODEL = "Latitude 5540";
    private static final EquipmentState DEFAULT_STATE = EquipmentState.AVAILABLE;
    private static final ConditionScore DEFAULT_CONDITION = ConditionScore.of(0.9);
    private static final LocalDate DEFAULT_PURCHASE_DATE = LocalDate.of(2024, 1, 15);

    @Test
    void shouldCreateEquipmentWhenAllFieldsValid() {
        // when
        var equipment = new Equipment(
                EQUIPMENT_ID, DEFAULT_TYPE, DEFAULT_BRAND, DEFAULT_MODEL,
                DEFAULT_STATE, DEFAULT_CONDITION, DEFAULT_PURCHASE_DATE, null
        );

        // then
        assertThat(equipment.id()).isEqualTo(EQUIPMENT_ID);
        assertThat(equipment.type()).isEqualTo(DEFAULT_TYPE);
        assertThat(equipment.brand()).isEqualTo(DEFAULT_BRAND);
        assertThat(equipment.model()).isEqualTo(DEFAULT_MODEL);
        assertThat(equipment.state()).isEqualTo(DEFAULT_STATE);
        assertThat(equipment.conditionScore()).isEqualTo(DEFAULT_CONDITION);
        assertThat(equipment.purchaseDate()).isEqualTo(DEFAULT_PURCHASE_DATE);
        assertThat(equipment.retireReason()).isNull();
    }

    @Test
    void shouldCreateEquipmentWhenRetireReasonProvided() {
        // when
        var equipment = new Equipment(
                EQUIPMENT_ID, DEFAULT_TYPE, DEFAULT_BRAND, DEFAULT_MODEL,
                EquipmentState.RETIRED, DEFAULT_CONDITION, DEFAULT_PURCHASE_DATE, "End of life"
        );

        // then
        assertThat(equipment.retireReason()).isEqualTo("End of life");
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideNullFieldCases")
    void shouldThrowWhenRequiredFieldNull(String fieldName, UUID id, EquipmentType type, String brand,
                                          String model, EquipmentState state, ConditionScore conditionScore,
                                          LocalDate purchaseDate) {
        // then
        assertThatThrownBy(() -> new Equipment(id, type, brand, model, state, conditionScore, purchaseDate, null))
                .isInstanceOf(NullPointerException.class);
    }

    private static Stream<Arguments> provideNullFieldCases() {
        var id = UUID.randomUUID();
        var type = EquipmentType.MAIN_COMPUTER;
        var brand = "Dell";
        var model = "Latitude 5540";
        var state = EquipmentState.AVAILABLE;
        var condition = ConditionScore.of(0.9);
        var purchaseDate = LocalDate.of(2024, 1, 15);

        return Stream.of(
                Arguments.of("null id", null, type, brand, model, state, condition, purchaseDate),
                Arguments.of("null type", id, null, brand, model, state, condition, purchaseDate),
                Arguments.of("null brand", id, type, null, model, state, condition, purchaseDate),
                Arguments.of("null model", id, type, brand, null, state, condition, purchaseDate),
                Arguments.of("null state", id, type, brand, model, null, condition, purchaseDate),
                Arguments.of("null conditionScore", id, type, brand, model, state, null, purchaseDate),
                Arguments.of("null purchaseDate", id, type, brand, model, state, condition, null)
        );
    }
}
