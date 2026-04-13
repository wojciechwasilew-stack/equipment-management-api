package com.tequipy.equipment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tequipy.equipment.application.port.out.PublishAllocationEventPort;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(BaseIntegrationTest.TestInfrastructureConfig.class)
abstract class BaseIntegrationTest {

    @TestConfiguration
    static class TestInfrastructureConfig {

        @Bean
        @Primary
        CacheManager noOpCacheManager() {
            return new NoOpCacheManager();
        }

        @Bean
        @Primary
        RabbitTemplate rabbitTemplate() {
            return mock(RabbitTemplate.class);
        }

        @Bean
        @Primary
        ConnectionFactory connectionFactory() {
            return mock(ConnectionFactory.class);
        }

        @Bean
        @Primary
        RedisConnectionFactory redisConnectionFactory() {
            var factory = mock(RedisConnectionFactory.class);
            var connection = mock(org.springframework.data.redis.connection.RedisConnection.class);
            given(connection.ping()).willReturn("PONG");
            given(factory.getConnection()).willReturn(connection);
            return factory;
        }

        @Bean
        @Primary
        PublishAllocationEventPort publishAllocationEventPort() {
            return mock(PublishAllocationEventPort.class);
        }

    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String registerEquipment(String type, String brand, String model,
                                       double conditionScore, String purchaseDate) throws Exception {
        var request = Map.of(
                "type", type,
                "brand", brand,
                "model", model,
                "conditionScore", conditionScore,
                "purchaseDate", purchaseDate
        );
        var result = mockMvc.perform(post("/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        var responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseBody.get("id").asText();
    }

    @SuppressWarnings("unchecked")
    protected String createAllocation(String employeeId, List<Map<String, Object>> policyItems) throws Exception {
        var request = Map.of(
                "employeeId", employeeId,
                "policyItems", policyItems
        );
        var result = mockMvc.perform(post("/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        var responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseBody.get("id").asText();
    }

    protected String getAllocationState(String allocationId) throws Exception {
        var result = mockMvc.perform(get("/allocations/{id}", allocationId))
                .andExpect(status().isOk())
                .andReturn();
        var responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseBody.get("state").asText();
    }

    protected Map<String, Object> buildPolicyItem(String equipmentType, int quantity,
                                                   double minimumConditionScore,
                                                   String preferredBrand, boolean preferRecent) {
        var map = new java.util.HashMap<String, Object>();
        map.put("equipmentType", equipmentType);
        map.put("quantity", quantity);
        map.put("minimumConditionScore", minimumConditionScore);
        if (preferredBrand != null) {
            map.put("preferredBrand", preferredBrand);
        }
        map.put("preferRecent", preferRecent);
        return map;
    }
}
