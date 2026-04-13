package com.tequipy.equipment.adapter.out.messaging;

import com.tequipy.equipment.application.port.out.PublishAllocationEventPort;
import com.tequipy.equipment.domain.event.AllocationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationEventPublisherAdapter implements PublishAllocationEventPort {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.allocation}")
    private String allocationExchange;

    @Value("${rabbitmq.routing-key.allocation-created}")
    private String allocationCreatedRoutingKey;

    @Override
    public void publish(AllocationCreatedEvent event) {
        rabbitTemplate.convertAndSend(allocationExchange, allocationCreatedRoutingKey, event.allocationRequestId().toString());
        log.info("Published AllocationCreatedEvent for request {}", event.allocationRequestId());
    }
}
