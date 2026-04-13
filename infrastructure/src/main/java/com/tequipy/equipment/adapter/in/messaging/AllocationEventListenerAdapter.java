package com.tequipy.equipment.adapter.in.messaging;

import com.tequipy.equipment.application.port.in.ProcessAllocationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationEventListenerAdapter {

    private final ProcessAllocationUseCase processAllocationUseCase;

    @RabbitListener(queues = "${rabbitmq.queue.allocation-created}")
    public void handleAllocationCreated(String allocationRequestId) {
        log.info("Received AllocationCreatedEvent for request {}", allocationRequestId);
        processAllocationUseCase.process(UUID.fromString(allocationRequestId));
    }
}
