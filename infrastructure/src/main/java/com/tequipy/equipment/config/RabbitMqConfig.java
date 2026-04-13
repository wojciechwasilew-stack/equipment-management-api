package com.tequipy.equipment.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.queue.allocation-created}")
    private String allocationCreatedQueue;

    @Value("${rabbitmq.exchange.allocation}")
    private String allocationExchange;

    @Value("${rabbitmq.routing-key.allocation-created}")
    private String allocationCreatedRoutingKey;

    @Bean
    public Queue allocationCreatedQueue() {
        return new Queue(allocationCreatedQueue, true);
    }

    @Bean
    public TopicExchange allocationExchange() {
        return new TopicExchange(allocationExchange);
    }

    @Bean
    public Binding allocationCreatedBinding() {
        return BindingBuilder.bind(allocationCreatedQueue())
                .to(allocationExchange())
                .with(allocationCreatedRoutingKey);
    }
}
