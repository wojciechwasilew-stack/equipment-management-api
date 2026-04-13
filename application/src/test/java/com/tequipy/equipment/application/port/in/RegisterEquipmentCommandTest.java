package com.tequipy.equipment.application.port.in;

import com.tequipy.equipment.domain.model.EquipmentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterEquipmentCommandTest {

    @Test
    void shouldCreateCommandWhenAllFieldsValid() {
        // when
        var command = new RegisterEquipmentCommand(
                EquipmentType.MAIN_COMPUTER, "Dell", "Latitude 5540",
                BigDecimal.valueOf(0.9), LocalDate.of(2024, 6, 1)
        );

        // then
        assertThat(command.type()).isEqualTo(EquipmentType.MAIN_COMPUTER);
        assertThat(command.brand()).isEqualTo("Dell");
        assertThat(command.model()).isEqualTo("Latitude 5540");
        assertThat(command.conditionScore()).isEqualByComparingTo(BigDecimal.valueOf(0.9));
        assertThat(command.purchaseDate()).isEqualTo(LocalDate.of(2024, 6, 1));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideNullFieldCases")
    void shouldThrowWhenRequiredFieldNull(String fieldName, EquipmentType type, String brand, String model,
                                           BigDecimal conditionScore, LocalDate purchaseDate) {
        assertThatThrownBy(() -> new RegisterEquipmentCommand(type, brand, model, conditionScore, purchaseDate))
                .isInstanceOf(NullPointerException.class);
    }

    private static Stream<Arguments> provideNullFieldCases() {
        var type = EquipmentType.MAIN_COMPUTER;
        var brand = "Dell";
        var model = "Latitude 5540";
        var conditionScore = BigDecimal.valueOf(0.9);
        var purchaseDate = LocalDate.of(2024, 6, 1);

        return Stream.of(
                Arguments.of("null type", null, brand, model, conditionScore, purchaseDate),
                Arguments.of("null brand", type, null, model, conditionScore, purchaseDate),
                Arguments.of("null model", type, brand, null, conditionScore, purchaseDate),
                Arguments.of("null conditionScore", type, brand, model, null, purchaseDate),
                Arguments.of("null purchaseDate", type, brand, model, conditionScore, null)
        );
    }
}
