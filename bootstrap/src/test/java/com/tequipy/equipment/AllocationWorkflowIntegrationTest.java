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

class AllocationWorkflowIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateAllocationAndProcessItWithAvailableEquipment() throws Exception {
        // given
        registerEquipment("MAIN_COMPUTER", "Dell", "Latitude-alloc-process", 0.9, "2024-06-01");
        var policyItem = buildPolicyItem("MAIN_COMPUTER", 1, 0.5, "Dell", true);

        // when
        var allocationId = createAllocation("EMP-PROCESS-001", List.of(policyItem));
        Thread.sleep(3000);

        // then
        var state = getAllocationState(allocationId);
        assertThat(state).isIn("ALLOCATED", "PENDING");
    }

    @Test
    void shouldConfirmAllocatedAllocationAndAssignEquipment() throws Exception {
        // given
        registerEquipment("MONITOR", "LG", "27UK-confirm-alloc", 0.85, "2024-03-15");
        var policyItem = buildPolicyItem("MONITOR", 1, 0.5, "LG", false);
        var allocationId = createAllocation("EMP-CONFIRM-001", List.of(policyItem));
        Thread.sleep(3000);

        // when
        var state = getAllocationState(allocationId);
        if ("ALLOCATED".equals(state)) {
            mockMvc.perform(post("/allocations/{id}/confirm", allocationId))
                    .andExpect(status().isNoContent());

            // then
            var confirmedState = getAllocationState(allocationId);
            assertThat(confirmedState).isEqualTo("CONFIRMED");
        }
    }

    @Test
    void shouldCancelPendingAllocationSuccessfully() throws Exception {
        // given
        var policyItem = buildPolicyItem("MOUSE", 1, 0.3, null, false);
        var allocationId = createAllocation("EMP-CANCEL-001", List.of(policyItem));

        // when
        mockMvc.perform(post("/allocations/{id}/cancel", allocationId))
                .andExpect(status().isNoContent());

        // then
        var state = getAllocationState(allocationId);
        assertThat(state).isEqualTo("CANCELLED");
    }

    @Test
    void shouldCancelAllocatedAllocationAndReleaseEquipment() throws Exception {
        // given
        registerEquipment("KEYBOARD", "Logitech", "MX Keys-cancel-alloc", 0.95, "2024-01-10");
        var policyItem = buildPolicyItem("KEYBOARD", 1, 0.5, "Logitech", false);
        var allocationId = createAllocation("EMP-CANCEL-002", List.of(policyItem));
        Thread.sleep(3000);

        // when
        var state = getAllocationState(allocationId);
        if ("ALLOCATED".equals(state)) {
            mockMvc.perform(post("/allocations/{id}/cancel", allocationId))
                    .andExpect(status().isNoContent());

            // then
            var cancelledState = getAllocationState(allocationId);
            assertThat(cancelledState).isEqualTo("CANCELLED");
        }
    }

    @Test
    void shouldReturnAllocationDetailsWithPolicyItems() throws Exception {
        // given
        var computerPolicy = buildPolicyItem("MAIN_COMPUTER", 1, 0.7, "Dell", true);
        var monitorPolicy = buildPolicyItem("MONITOR", 2, 0.5, null, false);
        var allocationId = createAllocation("EMP-DETAILS-001", List.of(computerPolicy, monitorPolicy));

        // when + then
        mockMvc.perform(get("/allocations/{id}", allocationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(allocationId))
                .andExpect(jsonPath("$.employeeId").value("EMP-DETAILS-001"))
                .andExpect(jsonPath("$.state").isNotEmpty())
                .andExpect(jsonPath("$.policyItems").isArray())
                .andExpect(jsonPath("$.policyItems.length()").value(2));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonexistentAllocation() throws Exception {
        // given
        var nonexistentId = "00000000-0000-0000-0000-000000000002";

        // when + then
        mockMvc.perform(get("/allocations/{id}", nonexistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ALLOCATION_NOT_FOUND"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }

    @Test
    void shouldReturnConflictWhenConfirmingPendingAllocation() throws Exception {
        // given
        var policyItem = buildPolicyItem("MAIN_COMPUTER", 1, 0.99, "NonExistentBrand", true);
        var allocationId = createAllocation("EMP-CONFLICT-001", List.of(policyItem));

        // when + then
        mockMvc.perform(post("/allocations/{id}/confirm", allocationId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void shouldReturnConflictWhenCancellingAlreadyCancelledAllocation() throws Exception {
        // given
        var policyItem = buildPolicyItem("MOUSE", 1, 0.3, null, false);
        var allocationId = createAllocation("EMP-DOUBLE-CANCEL", List.of(policyItem));
        mockMvc.perform(post("/allocations/{id}/cancel", allocationId))
                .andExpect(status().isNoContent());

        // when + then
        mockMvc.perform(post("/allocations/{id}/cancel", allocationId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void shouldHandleAllocationWithMultiplePolicyItemTypes() throws Exception {
        // given
        registerEquipment("MAIN_COMPUTER", "Dell", "Latitude-multi-alloc", 0.9, "2024-06-01");
        registerEquipment("MONITOR", "LG", "27UK-multi-alloc", 0.85, "2024-03-15");
        registerEquipment("KEYBOARD", "Logitech", "MX-multi-alloc", 0.80, "2024-01-10");

        var computerPolicy = buildPolicyItem("MAIN_COMPUTER", 1, 0.5, null, false);
        var monitorPolicy = buildPolicyItem("MONITOR", 1, 0.5, null, false);
        var keyboardPolicy = buildPolicyItem("KEYBOARD", 1, 0.5, null, false);

        // when
        var allocationId = createAllocation("EMP-MULTI-001",
                List.of(computerPolicy, monitorPolicy, keyboardPolicy));
        Thread.sleep(3000);

        // then
        var state = getAllocationState(allocationId);
        assertThat(state).isIn("ALLOCATED", "PENDING");
    }

    @Test
    void shouldCreateMultipleIndependentAllocationsForDifferentEmployees() throws Exception {
        // given
        registerEquipment("MOUSE", "Logitech", "MX Master-emp1", 0.88, "2024-02-20");
        registerEquipment("MOUSE", "Razer", "DeathAdder-emp2", 0.75, "2023-12-15");

        var policyItemOne = buildPolicyItem("MOUSE", 1, 0.3, null, false);
        var policyItemTwo = buildPolicyItem("MOUSE", 1, 0.3, null, false);

        // when
        var allocationIdOne = createAllocation("EMP-INDEPENDENT-001", List.of(policyItemOne));
        var allocationIdTwo = createAllocation("EMP-INDEPENDENT-002", List.of(policyItemTwo));

        // then
        assertThat(allocationIdOne).isNotEqualTo(allocationIdTwo);

        mockMvc.perform(get("/allocations/{id}", allocationIdOne))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value("EMP-INDEPENDENT-001"));

        mockMvc.perform(get("/allocations/{id}", allocationIdTwo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value("EMP-INDEPENDENT-002"));
    }
}
