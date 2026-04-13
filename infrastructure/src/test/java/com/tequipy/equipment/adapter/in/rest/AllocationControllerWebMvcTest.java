package com.tequipy.equipment.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tequipy.equipment.adapter.in.rest.dto.AllocationResponse;
import com.tequipy.equipment.adapter.in.rest.dto.CreateAllocationRequest;
import com.tequipy.equipment.adapter.in.rest.dto.PolicyItemRequest;
import com.tequipy.equipment.adapter.in.rest.mapper.AllocationRestMapper;
import com.tequipy.equipment.application.port.in.CancelAllocationUseCase;
import com.tequipy.equipment.application.port.in.ConfirmAllocationUseCase;
import com.tequipy.equipment.application.port.in.CreateAllocationCommand;
import com.tequipy.equipment.application.port.in.CreateAllocationUseCase;
import com.tequipy.equipment.application.port.in.GetAllocationUseCase;
import com.tequipy.equipment.domain.exception.AllocationNotFoundException;
import com.tequipy.equipment.domain.exception.InvalidAllocationStateTransitionException;
import com.tequipy.equipment.domain.model.AllocationRequest;
import com.tequipy.equipment.domain.model.AllocationState;
import com.tequipy.equipment.domain.model.ConditionScore;
import com.tequipy.equipment.domain.model.EquipmentType;
import com.tequipy.equipment.domain.model.PolicyItem;
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

@WebMvcTest(controllers = AllocationController.class)
class AllocationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateAllocationUseCase createAllocationUseCase;

    @MockitoBean
    private GetAllocationUseCase getAllocationUseCase;

    @MockitoBean
    private ConfirmAllocationUseCase confirmAllocationUseCase;

    @MockitoBean
    private CancelAllocationUseCase cancelAllocationUseCase;

    @MockitoBean
    private AllocationRestMapper allocationRestMapper;

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
    void shouldReturnCreatedWhenCreatingValidAllocation() throws Exception {
        // given
        var allocationId = UUID.randomUUID();
        var policyItem = new PolicyItemRequest("MAIN_COMPUTER", 1, BigDecimal.valueOf(0.5), "Dell", true);
        var request = new CreateAllocationRequest("EMP-001", List.of(policyItem));
        given(allocationRestMapper.toCommand(any(CreateAllocationRequest.class)))
                .willReturn(new CreateAllocationCommand("EMP-001", List.of(
                        new CreateAllocationCommand.PolicyItemCommand(EquipmentType.MAIN_COMPUTER, 1,
                                BigDecimal.valueOf(0.5), "Dell", true))));
        given(createAllocationUseCase.create(any(CreateAllocationCommand.class))).willReturn(allocationId);

        // when + then
        mockMvc.perform(post("/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(allocationId.toString()));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAllocationWithMissingEmployeeId() throws Exception {
        // given
        var policyItem = new PolicyItemRequest("MAIN_COMPUTER", 1, BigDecimal.valueOf(0.5), null, false);
        var request = new CreateAllocationRequest("", List.of(policyItem));

        // when + then
        mockMvc.perform(post("/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnAllocationDetailsWhenGettingById() throws Exception {
        // given
        var allocationId = UUID.randomUUID();
        var allocationRequest = createAllocationRequest(allocationId, AllocationState.PENDING);
        var response = new AllocationResponse(allocationId, "EMP-001", "PENDING",
                List.of(new AllocationResponse.PolicyItemResponse("MAIN_COMPUTER", 1,
                        BigDecimal.valueOf(0.5), "Dell", true)),
                List.of());
        given(getAllocationUseCase.getById(allocationId)).willReturn(allocationRequest);
        given(allocationRestMapper.toResponse(allocationRequest)).willReturn(response);

        // when + then
        mockMvc.perform(get("/allocations/{id}", allocationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(allocationId.toString()))
                .andExpect(jsonPath("$.employeeId").value("EMP-001"))
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonexistentAllocation() throws Exception {
        // given
        var allocationId = UUID.randomUUID();
        given(getAllocationUseCase.getById(allocationId))
                .willThrow(new AllocationNotFoundException(allocationId));

        // when + then
        mockMvc.perform(get("/allocations/{id}", allocationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ALLOCATION_NOT_FOUND"));
    }

    @Test
    void shouldReturnNoContentWhenConfirmingAllocation() throws Exception {
        // given
        var allocationId = UUID.randomUUID();

        // when + then
        mockMvc.perform(post("/allocations/{id}/confirm", allocationId))
                .andExpect(status().isNoContent());

        then(confirmAllocationUseCase).should().confirm(allocationId);
    }

    @Test
    void shouldReturnConflictWhenConfirmingInvalidStateAllocation() throws Exception {
        // given
        var allocationId = UUID.randomUUID();
        doThrow(new InvalidAllocationStateTransitionException(allocationId, AllocationState.PENDING, AllocationState.CONFIRMED))
                .when(confirmAllocationUseCase).confirm(allocationId);

        // when + then
        mockMvc.perform(post("/allocations/{id}/confirm", allocationId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void shouldReturnNoContentWhenCancellingAllocation() throws Exception {
        // given
        var allocationId = UUID.randomUUID();

        // when + then
        mockMvc.perform(post("/allocations/{id}/cancel", allocationId))
                .andExpect(status().isNoContent());

        then(cancelAllocationUseCase).should().cancel(allocationId);
    }

    private AllocationRequest createAllocationRequest(UUID allocationId, AllocationState state) {
        var policyItem = new PolicyItem(EquipmentType.MAIN_COMPUTER, 1,
                ConditionScore.of(0.5), "Dell", true);
        return new AllocationRequest(allocationId, "EMP-001", List.of(policyItem), state, List.of());
    }
}
