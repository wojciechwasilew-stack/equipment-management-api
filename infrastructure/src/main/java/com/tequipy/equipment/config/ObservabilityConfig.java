package com.tequipy.equipment.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class ObservabilityConfig {

    @Bean
    public Counter equipmentRegisteredCounter(MeterRegistry meterRegistry) {
        return Counter.builder("equipment.registered")
                .description("Number of equipment items registered")
                .register(meterRegistry);
    }

    @Bean
    public Counter allocationCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("allocation.created")
                .description("Number of allocation requests created")
                .register(meterRegistry);
    }

    @Bean
    public Counter allocationConfirmedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("allocation.confirmed")
                .description("Number of allocation requests confirmed")
                .register(meterRegistry);
    }

    @Bean
    public Timer allocationProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("allocation.processing.duration")
                .description("Time taken to process allocation requests")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    @Bean
    public HealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return () -> {
            try (var connection = dataSource.getConnection()) {
                if (connection.isValid(2)) {
                    return Health.up().withDetail("database", "PostgreSQL").build();
                }
                return Health.down().withDetail("database", "Connection invalid").build();
            } catch (SQLException exception) {
                return Health.down().withDetail("database", exception.getMessage()).build();
            }
        };
    }

    @Bean
    public HealthIndicator redisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        return () -> {
            try {
                var connection = redisConnectionFactory.getConnection();
                var pong = connection.ping();
                connection.close();
                if ("PONG".equals(pong)) {
                    return Health.up().withDetail("redis", "Available").build();
                }
                return Health.down().withDetail("redis", "Ping failed").build();
            } catch (Exception exception) {
                return Health.down().withDetail("redis", exception.getMessage()).build();
            }
        };
    }
}
