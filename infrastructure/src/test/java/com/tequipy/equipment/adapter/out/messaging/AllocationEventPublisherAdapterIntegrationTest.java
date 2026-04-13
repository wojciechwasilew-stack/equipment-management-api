package com.tequipy.equipment.adapter.out.messaging;

import com.tequipy.equipment.domain.event.AllocationCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AllocationEventPublisherAdapterIntegrationTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AllocationEventPublisherAdapter allocationEventPublisherAdapter;

    @Test
    void shouldPublishAllocationCreatedEventToRabbitMq() {
        // given
        ReflectionTestUtils.setField(allocationEventPublisherAdapter, "allocationExchange", "allocation.exchange");
        ReflectionTestUtils.setField(allocationEventPublisherAdapter, "allocationCreatedRoutingKey", "allocation.created");
        var allocationRequestId = UUID.randomUUID();
        var event = new AllocationCreatedEvent(allocationRequestId);

        // when
        allocationEventPublisherAdapter.publish(event);

        // then
        then(rabbitTemplate).should().convertAndSend(
                "allocation.exchange",
                "allocation.created",
                allocationRequestId.toString()
        );
    }

    @Test
    void shouldPublishMultipleEventsToQueue() {
        // given
        ReflectionTestUtils.setField(allocationEventPublisherAdapter, "allocationExchange", "allocation.exchange");
        ReflectionTestUtils.setField(allocationEventPublisherAdapter, "allocationCreatedRoutingKey", "allocation.created");
        var firstAllocationId = UUID.randomUUID();
        var secondAllocationId = UUID.randomUUID();

        // when
        allocationEventPublisherAdapter.publish(new AllocationCreatedEvent(firstAllocationId));
        allocationEventPublisherAdapter.publish(new AllocationCreatedEvent(secondAllocationId));

        // then
        then(rabbitTemplate).should().convertAndSend(
                "allocation.exchange",
                "allocation.created",
                firstAllocationId.toString()
        );
        then(rabbitTemplate).should().convertAndSend(
                "allocation.exchange",
                "allocation.created",
                secondAllocationId.toString()
        );
    }
}
