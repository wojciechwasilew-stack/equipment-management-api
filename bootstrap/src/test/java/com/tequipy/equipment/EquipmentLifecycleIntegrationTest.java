package com.tequipy.equipment;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EquipmentLifecycleIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldRegisterEquipmentAndReturnCreatedWithId() throws Exception {
        // given
        var request = Map.of(
                "type", "MAIN_COMPUTER",
                "brand", "Dell",
                "model", "Latitude 5540",
                "conditionScore", 0.9,
                "purchaseDate", "2024-06-01"
        );

        // when + then
        mockMvc.perform(post("/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void shouldListRegisteredEquipmentByAvailableState() throws Exception {
        // given
        registerEquipment("MONITOR", "LG", "27UK850-lifecycle", 0.85, "2024-03-15");
        registerEquipment("MONITOR", "Samsung", "S27R650-lifecycle", 0.75, "2023-11-01");

        // when + then
        mockMvc.perform(get("/equipments").param("state", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void shouldRetireEquipmentAndTransitionToRetiredState() throws Exception {
        // given
        var equipmentId = registerEquipment("KEYBOARD", "Logitech", "MX Keys-lifecycle", 0.95, "2024-01-10");

        // when
        var retireRequest = Map.of("reason", "Keys stopped responding");
        mockMvc.perform(post("/equipments/{id}/retire", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retireRequest)))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/equipments").param("state", "RETIRED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='%s')].state".formatted(equipmentId), hasSize(1)));
    }

    @Test
    void shouldListAllEquipmentWithoutStateFilter() throws Exception {
        // given
        registerEquipment("MOUSE", "Logitech", "MX Master-lifecycle", 0.88, "2024-02-20");

        // when + then
        mockMvc.perform(get("/equipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void shouldReturnEquipmentWithCorrectFieldsAfterRegistration() throws Exception {
        // given
        var equipmentId = registerEquipment("MAIN_COMPUTER", "HP", "EliteBook 840-lifecycle", 0.78, "2024-04-15");

        // when
        var result = mockMvc.perform(get("/equipments").param("state", "AVAILABLE"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        var matchingEquipment = java.util.stream.StreamSupport.stream(responseBody.spliterator(), false)
                .filter(node -> equipmentId.equals(node.get("id").asText()))
                .findFirst();
        org.assertj.core.api.Assertions.assertThat(matchingEquipment).isPresent();

        var equipment = matchingEquipment.get();
        org.assertj.core.api.Assertions.assertThat(equipment.get("type").asText()).isEqualTo("MAIN_COMPUTER");
        org.assertj.core.api.Assertions.assertThat(equipment.get("brand").asText()).isEqualTo("HP");
        org.assertj.core.api.Assertions.assertThat(equipment.get("model").asText()).isEqualTo("EliteBook 840-lifecycle");
        org.assertj.core.api.Assertions.assertThat(equipment.get("state").asText()).isEqualTo("AVAILABLE");
        org.assertj.core.api.Assertions.assertThat(equipment.get("conditionScore").asDouble()).isEqualTo(0.78);
        org.assertj.core.api.Assertions.assertThat(equipment.get("purchaseDate").asText()).isEqualTo("2024-04-15");
    }

    @Test
    void shouldNotRetireAlreadyRetiredEquipment() throws Exception {
        // given
        var equipmentId = registerEquipment("MOUSE", "Razer", "DeathAdder-lifecycle", 0.6, "2023-09-01");
        var retireRequest = Map.of("reason", "Scroll wheel broken");
        mockMvc.perform(post("/equipments/{id}/retire", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retireRequest)))
                .andExpect(status().isNoContent());

        // when + then
        mockMvc.perform(post("/equipments/{id}/retire", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "Already retired"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void shouldReturnNotFoundWhenRetiringNonexistentEquipment() throws Exception {
        // given
        var nonexistentId = "00000000-0000-0000-0000-000000000001";
        var retireRequest = Map.of("reason", "Test reason");

        // when + then
        mockMvc.perform(post("/equipments/{id}/retire", nonexistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retireRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("EQUIPMENT_NOT_FOUND"));
    }

    @Test
    void shouldRegisterMultipleEquipmentOfDifferentTypes() throws Exception {
        // given
        registerEquipment("MAIN_COMPUTER", "Dell", "Latitude-multi", 0.9, "2024-06-01");
        registerEquipment("MONITOR", "LG", "27UK-multi", 0.85, "2024-03-15");
        registerEquipment("KEYBOARD", "Logitech", "MX-multi", 0.95, "2024-01-10");
        registerEquipment("MOUSE", "Logitech", "MX-multi-mouse", 0.88, "2024-02-20");

        // when
        var result = mockMvc.perform(get("/equipments"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        org.assertj.core.api.Assertions.assertThat(responseBody.size()).isGreaterThanOrEqualTo(4);
    }
}
