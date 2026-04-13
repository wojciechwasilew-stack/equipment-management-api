package com.tequipy.equipment;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityHeadersIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldIncludeSecurityHeadersOnGetRequest() throws Exception {
        // when + then
        mockMvc.perform(get("/equipments"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void shouldIncludeSecurityHeadersOnPostRequest() throws Exception {
        // given
        var request = Map.of(
                "type", "MAIN_COMPUTER",
                "brand", "Dell",
                "model", "Latitude-sec-header",
                "conditionScore", 0.9,
                "purchaseDate", "2024-06-01"
        );

        // when + then
        mockMvc.perform(post("/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void shouldIncludeSecurityHeadersOnErrorResponse() throws Exception {
        // given
        var nonexistentId = "00000000-0000-0000-0000-000000000006";

        // when + then
        mockMvc.perform(get("/allocations/{id}", nonexistentId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void shouldIncludeContentSecurityPolicyHeader() throws Exception {
        // when + then
        mockMvc.perform(get("/equipments"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void shouldAllowCorsForSwaggerAndApiEndpoints() throws Exception {
        // when + then
        mockMvc.perform(get("/equipments")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotExposeStackTracesInErrorResponses() throws Exception {
        // given
        var nonexistentId = "00000000-0000-0000-0000-000000000007";

        // when
        var result = mockMvc.perform(get("/allocations/{id}", nonexistentId))
                .andExpect(status().isNotFound())
                .andReturn();

        // then
        var responseBody = result.getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(responseBody).doesNotContain("stackTrace");
        org.assertj.core.api.Assertions.assertThat(responseBody).doesNotContain("java.lang");
        org.assertj.core.api.Assertions.assertThat(responseBody).doesNotContain(".java:");
    }
}
