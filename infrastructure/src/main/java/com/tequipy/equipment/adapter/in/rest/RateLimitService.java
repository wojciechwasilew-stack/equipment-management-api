package com.tequipy.equipment.adapter.in.rest;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();
    private final int requestsPerMinute;

    public RateLimitService(@Value("${rate-limit.requests-per-minute:100}") int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public Bucket resolveBucket(String clientIpAddress) {
        return bucketCache.computeIfAbsent(clientIpAddress, this::createNewBucket);
    }

    private Bucket createNewBucket(String clientIpAddress) {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(requestsPerMinute, Duration.ofMinutes(1)))
                .build();
    }
}
