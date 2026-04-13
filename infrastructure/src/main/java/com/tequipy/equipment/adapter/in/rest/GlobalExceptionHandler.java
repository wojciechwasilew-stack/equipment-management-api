package com.tequipy.equipment.adapter.in.rest;

import com.tequipy.equipment.domain.exception.AllocationFailedException;
import com.tequipy.equipment.domain.exception.AllocationNotFoundException;
import com.tequipy.equipment.domain.exception.EquipmentNotFoundException;
import com.tequipy.equipment.domain.exception.InvalidAllocationStateTransitionException;
import com.tequipy.equipment.domain.exception.InvalidEquipmentStateTransitionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EquipmentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEquipmentNotFound(EquipmentNotFoundException exception) {
        var correlationId = UUID.randomUUID().toString();
        log.error("Equipment not found [correlationId={}]: {}", correlationId, exception.getMessage());
        return new ApiError(Instant.now(), 404, ErrorCode.EQUIPMENT_NOT_FOUND, exception.getMessage(), correlationId);
    }

    @ExceptionHandler(AllocationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleAllocationNotFound(AllocationNotFoundException exception) {
        var correlationId = UUID.randomUUID().toString();
        log.error("Allocation not found [correlationId={}]: {}", correlationId, exception.getMessage());
        return new ApiError(Instant.now(), 404, ErrorCode.ALLOCATION_NOT_FOUND, exception.getMessage(), correlationId);
    }

    @ExceptionHandler({InvalidEquipmentStateTransitionException.class, InvalidAllocationStateTransitionException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleInvalidStateTransition(RuntimeException exception) {
        var correlationId = UUID.randomUUID().toString();
        log.error("Invalid state transition [correlationId={}]: {}", correlationId, exception.getMessage());
        return new ApiError(Instant.now(), 409, ErrorCode.INVALID_STATE_TRANSITION, exception.getMessage(), correlationId);
    }

    @ExceptionHandler(AllocationFailedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiError handleAllocationFailed(AllocationFailedException exception) {
        var correlationId = UUID.randomUUID().toString();
        log.error("Allocation failed [correlationId={}]: {}", correlationId, exception.getMessage());
        return new ApiError(Instant.now(), 422, ErrorCode.ALLOCATION_FAILED, exception.getMessage(), correlationId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException exception) {
        var correlationId = UUID.randomUUID().toString();
        var message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("Validation error [correlationId={}]: {}", correlationId, message);
        return new ApiError(Instant.now(), 400, ErrorCode.VALIDATION_ERROR, message, correlationId);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        var correlationId = UUID.randomUUID().toString();
        log.error("Malformed request body [correlationId={}]: {}", correlationId, exception.getMessage());
        return new ApiError(Instant.now(), 400, ErrorCode.VALIDATION_ERROR, "Malformed request body", correlationId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException exception) {
        var correlationId = UUID.randomUUID().toString();
        log.error("Bad request [correlationId={}]: {}", correlationId, exception.getMessage());
        return new ApiError(Instant.now(), 400, ErrorCode.VALIDATION_ERROR, exception.getMessage(), correlationId);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGenericException(Exception exception) {
        var correlationId = UUID.randomUUID().toString();
        log.error("Unexpected error [correlationId={}]", correlationId, exception);
        return new ApiError(Instant.now(), 500, ErrorCode.VALIDATION_ERROR, "An unexpected error occurred", correlationId);
    }
}
