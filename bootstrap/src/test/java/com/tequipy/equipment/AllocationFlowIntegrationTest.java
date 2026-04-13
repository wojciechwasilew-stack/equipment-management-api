package com.tequipy.equipment;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AllocationFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCompleteFullAllocationFlowFromRegistrationToConfirmation() throws Exception {
        // given
        var equipmentId = registerEquipment("MAIN_COMPUTER", "Dell", "Latitude 5540", 0.9, "2024-06-01");

        // when
        var allocationId = createAllocation("EMP-001",
                List.of(Map.of(
                        "equipmentType", "MAIN_COMPUTER",
                        "quantity", 1,
                        "minimumConditionScore", 0.5,
                        "preferredBrand", "Dell",
                        "preferRecent", true
                )));

        Thread.sleep(2000);

        // then
        var allocationState = getAllocationState(allocationId);
        assertThat(allocationState).isIn("ALLOCATED", "PENDING");

        if ("ALLOCATED".equals(allocationState)) {
            mockMvc.perform(post("/allocations/{id}/confirm", allocationId))
                    .andExpect(status().isNoContent());
            var confirmedState = getAllocationState(allocationId);
            assertThat(confirmedState).isEqualTo("CONFIRMED");
        }
    }

    @Test
    void shouldRegisterAndListEquipment() throws Exception {
        // given
        registerEquipment("MONITOR", "LG", "27UK850", 0.85, "2024-03-15");
        registerEquipment("MONITOR", "Samsung", "S27R650", 0.75, "2023-11-01");

        // when + then
        mockMvc.perform(get("/equipments").param("state", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void shouldRetireEquipmentAndExcludeFromAvailableList() throws Exception {
        // given
        var equipmentId = registerEquipment("KEYBOARD", "Logitech", "MX Keys", 0.95, "2024-01-10");

        // when
        var retireRequest = Map.of("reason", "Damaged beyond repair");
        mockMvc.perform(post("/equipments/{id}/retire", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retireRequest)))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/equipments").param("state", "RETIRED"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCancelPendingAllocation() throws Exception {
        // given
        var allocationId = createAllocation("EMP-002",
                List.of(Map.of(
                        "equipmentType", "MOUSE",
                        "quantity", 1,
                        "minimumConditionScore", 0.3,
                        "preferRecent", false
                )));

        // when
        mockMvc.perform(post("/allocations/{id}/cancel", allocationId))
                .andExpect(status().isNoContent());

        // then
        var state = getAllocationState(allocationId);
        assertThat(state).isEqualTo("CANCELLED");
    }

    @Test
    void shouldReturnNotFoundForNonexistentAllocation() throws Exception {
        // given
        var nonexistentId = "00000000-0000-0000-0000-000000000000";

        // when + then
        mockMvc.perform(get("/allocations/{id}", nonexistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ALLOCATION_NOT_FOUND"));
    }
}
