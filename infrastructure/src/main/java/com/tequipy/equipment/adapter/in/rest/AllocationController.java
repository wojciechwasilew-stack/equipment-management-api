package com.tequipy.equipment.adapter.in.rest;

import com.tequipy.equipment.adapter.in.rest.dto.AllocationResponse;
import com.tequipy.equipment.adapter.in.rest.dto.CreateAllocationRequest;
import com.tequipy.equipment.adapter.in.rest.mapper.AllocationRestMapper;
import com.tequipy.equipment.application.port.in.CancelAllocationUseCase;
import com.tequipy.equipment.application.port.in.ConfirmAllocationUseCase;
import com.tequipy.equipment.application.port.in.CreateAllocationUseCase;
import com.tequipy.equipment.application.port.in.GetAllocationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Allocation", description = "Equipment allocation operations")
@RestController
@RequestMapping("/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private final CreateAllocationUseCase createAllocationUseCase;
    private final GetAllocationUseCase getAllocationUseCase;
    private final ConfirmAllocationUseCase confirmAllocationUseCase;
    private final CancelAllocationUseCase cancelAllocationUseCase;
    private final AllocationRestMapper allocationRestMapper;

    @Operation(summary = "Create allocation request")
    @ApiResponse(responseCode = "201", description = "Allocation request created")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> createAllocation(@Valid @RequestBody CreateAllocationRequest request) {
        var command = allocationRestMapper.toCommand(request);
        var allocationId = createAllocationUseCase.create(command);
        return Map.of("id", allocationId);
    }

    @Operation(summary = "Get allocation request by ID")
    @ApiResponse(responseCode = "200", description = "Allocation details retrieved")
    @GetMapping("/{id}")
    public AllocationResponse getAllocation(@PathVariable UUID id) {
        var allocationRequest = getAllocationUseCase.getById(id);
        return allocationRestMapper.toResponse(allocationRequest);
    }

    @Operation(summary = "Confirm allocation")
    @ApiResponse(responseCode = "204", description = "Allocation confirmed")
    @PostMapping("/{id}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmAllocation(@PathVariable UUID id) {
        confirmAllocationUseCase.confirm(id);
    }

    @Operation(summary = "Cancel allocation")
    @ApiResponse(responseCode = "204", description = "Allocation cancelled")
    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelAllocation(@PathVariable UUID id) {
        cancelAllocationUseCase.cancel(id);
    }
}
