package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResilienceHealth {
    private LocalDateTime timestamp;
    private String overallStatus;
    private Map<String, String> circuitBreakers;
    private Map<String, RateLimiterStatus> rateLimiters;
}
