package com.tequipy.equipment.adapter.in.rest;

import com.tequipy.equipment.adapter.in.rest.dto.EquipmentResponse;
import com.tequipy.equipment.adapter.in.rest.dto.RegisterEquipmentRequest;
import com.tequipy.equipment.adapter.in.rest.dto.RetireEquipmentRequest;
import com.tequipy.equipment.adapter.in.rest.mapper.EquipmentRestMapper;
import com.tequipy.equipment.application.port.in.ListEquipmentUseCase;
import com.tequipy.equipment.application.port.in.RegisterEquipmentUseCase;
import com.tequipy.equipment.application.port.in.RetireEquipmentUseCase;
import com.tequipy.equipment.domain.model.EquipmentState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Equipment", description = "Equipment management operations")
@RestController
@RequestMapping("/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final RegisterEquipmentUseCase registerEquipmentUseCase;
    private final ListEquipmentUseCase listEquipmentUseCase;
    private final RetireEquipmentUseCase retireEquipmentUseCase;
    private final EquipmentRestMapper equipmentRestMapper;

    @Operation(summary = "Register new equipment")
    @ApiResponse(responseCode = "201", description = "Equipment registered successfully")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> registerEquipment(@Valid @RequestBody RegisterEquipmentRequest request) {
        var command = equipmentRestMapper.toCommand(request);
        var equipmentId = registerEquipmentUseCase.register(command);
        return Map.of("id", equipmentId);
    }

    @Operation(summary = "List equipment with optional state filter")
    @ApiResponse(responseCode = "200", description = "Equipment list retrieved")
    @GetMapping
    public List<EquipmentResponse> listEquipment(
            @Parameter(description = "Filter by equipment state") @RequestParam(required = false) String state) {
        var equipmentList = (state != null)
                ? listEquipmentUseCase.listByState(EquipmentState.valueOf(state))
                : listEquipmentUseCase.listAll();
        return equipmentRestMapper.toResponseList(equipmentList);
    }

    @Operation(summary = "Retire equipment")
    @ApiResponse(responseCode = "204", description = "Equipment retired successfully")
    @PostMapping("/{id}/retire")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retireEquipment(@PathVariable UUID id, @Valid @RequestBody RetireEquipmentRequest request) {
        retireEquipmentUseCase.retire(id, request.reason());
    }
}
