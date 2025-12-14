package com.ecommerce.listener;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CircuitBreakerEventListener {
    
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    @PostConstruct
    public void registerEventListeners() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    log.warn("Circuit Breaker {} state changed from {} to {}", 
                        circuitBreaker.getName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()
                    );
                    
                    // Send alert to monitoring system
                    if (event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
                        sendAlert(circuitBreaker.getName(), "CIRCUIT_BREAKER_OPEN");
                    }
                })
                .onError(event -> {
                    log.error("Circuit Breaker {} recorded error: {}", 
                        circuitBreaker.getName(), 
                        event.getThrowable().getMessage()
                    );
                })
                .onSuccess(event -> {
                    log.debug("Circuit Breaker {} successful call", circuitBreaker.getName());
                });
        });
    }
    
    private void sendAlert(String circuitBreakerName, String alertType) {
        // Send to Slack, PagerDuty, etc.
        log.error("ALERT: {} for circuit breaker {}", alertType, circuitBreakerName);
    }
}
