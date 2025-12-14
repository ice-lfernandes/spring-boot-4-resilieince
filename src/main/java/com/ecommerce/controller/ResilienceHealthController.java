package com.ecommerce.controller;

import com.ecommerce.dto.RateLimiterStatus;
import com.ecommerce.dto.ResilienceHealth;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class ResilienceHealthController {
    
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    
    @GetMapping("/resilience")
    public ResponseEntity<ResilienceHealth> getResilienceHealth() {
        Map<String, String> circuitBreakers = new HashMap<>();
        Map<String, RateLimiterStatus> rateLimiters = new HashMap<>();
        
        // Circuit Breakers status
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            circuitBreakers.put(cb.getName(), cb.getState().name());
        });
        
        // Rate Limiters status
        rateLimiterRegistry.getAllRateLimiters().forEach(rl -> {
            RateLimiter.Metrics metrics = rl.getMetrics();
            rateLimiters.put(rl.getName(), RateLimiterStatus.builder()
                .availablePermissions(metrics.getAvailablePermissions())
                .numberOfWaitingThreads(metrics.getNumberOfWaitingThreads())
                .build());
        });
        
        return ResponseEntity.ok(ResilienceHealth.builder()
            .timestamp(LocalDateTime.now())
            .circuitBreakers(circuitBreakers)
            .rateLimiters(rateLimiters)
            .overallStatus(calculateOverallStatus(circuitBreakers))
            .build());
    }
    
    private String calculateOverallStatus(Map<String, String> circuitBreakers) {
        boolean hasOpenCircuits = circuitBreakers.values().stream()
            .anyMatch(state -> state.equals("OPEN"));
        
        return hasOpenCircuits ? "DEGRADED" : "HEALTHY";
    }
}
