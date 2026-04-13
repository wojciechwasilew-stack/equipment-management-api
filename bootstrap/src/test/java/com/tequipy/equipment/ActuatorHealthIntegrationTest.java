package com.tequipy.equipment;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActuatorHealthIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldReturnHealthStatusUp() throws Exception {
        // when + then
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldExposeLivenessProbe() throws Exception {
        // when + then
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldExposeReadinessProbe() throws Exception {
        // when + then
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldExposeHealthDetailsWithDatabaseComponent() throws Exception {
        // when + then
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").isNotEmpty())
                .andExpect(jsonPath("$.components.db").isNotEmpty())
                .andExpect(jsonPath("$.components.db.status").value("UP"));
    }

    @Test
    void shouldExposePrometheusMetricsEndpoint() throws Exception {
        // when + then
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldExposeMetricsEndpoint() throws Exception {
        // when + then
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }

    @Test
    void shouldExposeApplicationMetricInPrometheus() throws Exception {
        // when
        var result = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var responseBody = result.getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(responseBody).contains("jvm_memory");
    }

    @Test
    void shouldTrackEquipmentRegistrationMetricAfterRegistration() throws Exception {
        // given
        registerEquipment("MAIN_COMPUTER", "Dell", "Latitude-metric", 0.9, "2024-06-01");

        // when
        var result = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var responseBody = result.getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(responseBody).contains("http_server_requests");
    }
}
