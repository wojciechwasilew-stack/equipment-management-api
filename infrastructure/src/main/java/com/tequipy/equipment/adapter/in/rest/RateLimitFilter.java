package com.tequipy.equipment.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var clientIpAddress = request.getRemoteAddr();
        var bucket = rateLimitService.resolveBucket(clientIpAddress);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            var apiError = new ApiError(
                    Instant.now(),
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    ErrorCode.RATE_LIMIT_EXCEEDED,
                    "Rate limit exceeded. Please try again later.",
                    UUID.randomUUID().toString()
            );
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), apiError);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs");
    }
}
