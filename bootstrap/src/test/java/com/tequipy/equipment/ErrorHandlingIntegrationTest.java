package com.tequipy.equipment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ErrorHandlingIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldReturnBadRequestWhenRegisteringEquipmentWithNullType() throws Exception {
        // given
        var request = Map.of(
                "brand", "Dell",
                "model", "Latitude",
                "conditionScore", 0.9,
                "purchaseDate", "2024-06-01"
        );

        // when + then
        mockMvc.perform(post("/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenRegisteringEquipmentWithBlankBrand() throws Exception {
        // given
        var request = Map.of(
                "type", "MAIN_COMPUTER",
                "brand", "",
                "model", "Latitude",
                "conditionScore", 0.9,
                "purchaseDate", "2024-06-01"
        );

        // when + then
        mockMvc.perform(post("/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnBadRequestWhenRegisteringEquipmentWithInvalidConditionScore() throws Exception {
        // given
        var request = Map.of(
                "type", "MAIN_COMPUTER",
                "brand", "Dell",
                "model", "Latitude",
                "conditionScore", 1.5,
                "purchaseDate", "2024-06-01"
        );

        // when + then
        mockMvc.perform(post("/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRegisteringEquipmentWithInvalidType() throws Exception {
        // given
        var request = Map.of(
                "type", "INVALID_TYPE",
                "brand", "Dell",
                "model", "Latitude",
                "conditionScore", 0.9,
                "purchaseDate", "2024-06-01"
        );

        // when + then
        mockMvc.perform(post("/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAllocationWithEmptyEmployeeId() throws Exception {
        // given
        var request = Map.of(
                "employeeId", "",
                "policyItems", List.of(Map.of(
                        "equipmentType", "MAIN_COMPUTER",
                        "quantity", 1,
                        "minimumConditionScore", 0.5,
                        "preferRecent", false
                ))
        );

        // when + then
        mockMvc.perform(post("/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAllocationWithEmptyPolicyItems() throws Exception {
        // given
        var request = Map.of(
                "employeeId", "EMP-ERR-001",
                "policyItems", List.of()
        );

        // when + then
        mockMvc.perform(post("/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnBadRequestWhenRetiringEquipmentWithBlankReason() throws Exception {
        // given
        var equipmentId = registerEquipment("MOUSE", "Logitech", "MX-err-retire", 0.88, "2024-02-20");
        var retireRequest = Map.of("reason", "");

        // when + then
        mockMvc.perform(post("/equipments/{id}/retire", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retireRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnNotFoundWhenConfirmingNonexistentAllocation() throws Exception {
        // given
        var nonexistentId = "00000000-0000-0000-0000-000000000003";

        // when + then
        mockMvc.perform(post("/allocations/{id}/confirm", nonexistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ALLOCATION_NOT_FOUND"));
    }

    @Test
    void shouldReturnNotFoundWhenCancellingNonexistentAllocation() throws Exception {
        // given
        var nonexistentId = "00000000-0000-0000-0000-000000000004";

        // when + then
        mockMvc.perform(post("/allocations/{id}/cancel", nonexistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ALLOCATION_NOT_FOUND"));
    }

    @Test
    void shouldReturnStandardizedErrorFormatWithAllRequiredFields() throws Exception {
        // given
        var nonexistentId = "00000000-0000-0000-0000-000000000005";

        // when + then
        mockMvc.perform(get("/allocations/{id}", nonexistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("ALLOCATION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenSendingInvalidJsonBody() throws Exception {
        // when + then
        mockMvc.perform(post("/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenFilteringByInvalidState() throws Exception {
        // when + then
        mockMvc.perform(get("/equipments").param("state", "NONEXISTENT_STATE"))
                .andExpect(status().isBadRequest());
    }

    static Stream<Arguments> provideInvalidEquipmentRequests() {
        return Stream.of(
                Arguments.of("missing brand",
                        Map.of("type", "MAIN_COMPUTER", "model", "Test", "conditionScore", 0.9, "purchaseDate", "2024-01-01")),
                Arguments.of("negative condition score",
                        Map.of("type", "MAIN_COMPUTER", "brand", "Dell", "model", "Test", "conditionScore", -0.1, "purchaseDate", "2024-01-01"))
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideInvalidEquipmentRequests")
    void shouldReturnBadRequestForInvalidEquipmentRegistration(String scenario,
                                                                Map<String, Object> request) throws Exception {
        // when + then
        mockMvc.perform(post("/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
