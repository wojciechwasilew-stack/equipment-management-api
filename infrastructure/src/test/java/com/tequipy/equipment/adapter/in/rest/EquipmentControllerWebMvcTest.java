package com.tequipy.equipment.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tequipy.equipment.adapter.in.rest.dto.EquipmentResponse;
import com.tequipy.equipment.adapter.in.rest.dto.RegisterEquipmentRequest;
import com.tequipy.equipment.adapter.in.rest.dto.RetireEquipmentRequest;
import com.tequipy.equipment.adapter.in.rest.mapper.EquipmentRestMapper;
import com.tequipy.equipment.application.port.in.ListEquipmentUseCase;
import com.tequipy.equipment.application.port.in.RegisterEquipmentCommand;
import com.tequipy.equipment.application.port.in.RegisterEquipmentUseCase;
import com.tequipy.equipment.application.port.in.RetireEquipmentUseCase;
import com.tequipy.equipment.domain.exception.EquipmentNotFoundException;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.Equipment;
import com.tequipy.equipment.domain.model.EquipmentState;
import com.tequipy.equipment.domain.model.EquipmentType;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EquipmentController.class)
class EquipmentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterEquipmentUseCase registerEquipmentUseCase;

    @MockitoBean
    private ListEquipmentUseCase listEquipmentUseCase;

    @MockitoBean
    private RetireEquipmentUseCase retireEquipmentUseCase;

    @MockitoBean
    private EquipmentRestMapper equipmentRestMapper;

    @MockitoBean
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        var bucket = Bucket.builder()
                .addLimit(Bandwidth.simple(1000, Duration.ofMinutes(1)))
                .build();
        given(rateLimitService.resolveBucket(anyString())).willReturn(bucket);
    }

    @Test
    void shouldReturnCreatedWhenRegisteringValidEquipment() throws Exception {
        // given
        var equipmentId = UUID.randomUUID();
        var request = new RegisterEquipmentRequest("MAIN_COMPUTER", "Dell", "Latitude 5540",
                BigDecimal.valueOf(0.9), LocalDate.of(2024, 6, 1));
        given(equipmentRestMapper.toCommand(any(RegisterEquipmentRequest.class)))
                .willReturn(new RegisterEquipmentCommand(EquipmentType.MAIN_COMPUTER, "Dell", "Latitude 5540",
                        BigDecimal.valueOf(0.9), LocalDate.of(2024, 6, 1)));
        given(registerEquipmentUseCase.register(any(RegisterEquipmentCommand.class))).willReturn(equipmentId);

        // when + then
        mockMvc.perform(post("/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(equipmentId.toString()));
    }

    @Test
    void shouldReturnBadRequestWhenRegisteringWithMissingFields() throws Exception {
        // given
        var request = new RegisterEquipmentRequest(null, "", null, null, null);

        // when + then
        mockMvc.perform(post("/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnEquipmentListWhenListingWithoutFilter() throws Exception {
        // given
        var equipment = createEquipment();
        var response = new EquipmentResponse(equipment.id(), "MAIN_COMPUTER", "Dell", "Latitude 5540",
                "AVAILABLE", BigDecimal.valueOf(0.9), LocalDate.of(2024, 6, 1), null);
        given(listEquipmentUseCase.listAll()).willReturn(List.of(equipment));
        given(equipmentRestMapper.toResponseList(any())).willReturn(List.of(response));

        // when + then
        mockMvc.perform(get("/equipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("Dell"));
    }

    @Test
    void shouldReturnFilteredListWhenListingWithStateParam() throws Exception {
        // given
        var equipment = createEquipment();
        var response = new EquipmentResponse(equipment.id(), "MAIN_COMPUTER", "Dell", "Latitude 5540",
                "AVAILABLE", BigDecimal.valueOf(0.9), LocalDate.of(2024, 6, 1), null);
        given(listEquipmentUseCase.listByState(EquipmentState.AVAILABLE)).willReturn(List.of(equipment));
        given(equipmentRestMapper.toResponseList(any())).willReturn(List.of(response));

        // when + then
        mockMvc.perform(get("/equipments").param("state", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].state").value("AVAILABLE"));
    }

    @Test
    void shouldReturnNoContentWhenRetiringEquipment() throws Exception {
        // given
        var equipmentId = UUID.randomUUID();
        var request = new RetireEquipmentRequest("End of life");

        // when + then
        mockMvc.perform(post("/equipments/{id}/retire", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        then(retireEquipmentUseCase).should().retire(equipmentId, "End of life");
    }

    @Test
    void shouldReturnNotFoundWhenRetiringNonexistentEquipment() throws Exception {
        // given
        var equipmentId = UUID.randomUUID();
        var request = new RetireEquipmentRequest("End of life");
        doThrow(new EquipmentNotFoundException(equipmentId))
                .when(retireEquipmentUseCase).retire(equipmentId, "End of life");

        // when + then
        mockMvc.perform(post("/equipments/{id}/retire", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("EQUIPMENT_NOT_FOUND"));
    }

    @Test
    void shouldReturnBadRequestWhenRetiringWithBlankReason() throws Exception {
        // given
        var equipmentId = UUID.randomUUID();
        var request = new RetireEquipmentRequest("");

        // when + then
        mockMvc.perform(post("/equipments/{id}/retire", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    private Equipment createEquipment() {
        return new Equipment(UUID.randomUUID(), EquipmentType.MAIN_COMPUTER, "Dell", "Latitude 5540",
                EquipmentState.AVAILABLE, ConditionScore.of(0.9), LocalDate.of(2024, 6, 1), null);
    }
}
