package com.ecommerce.service;

import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.dto.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PaymentService {
    
    private final RestTemplate restTemplate;
    
    public PaymentService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }
    
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @TimeLimiter(name = "paymentService")
    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest request) {
        log.info("Attempting to process payment: {}", request.getOrderId());
        
        return CompletableFuture.supplyAsync(() -> {
            // Simulate payment processing
            // In real scenario: restTemplate.postForEntity("https://payment-gateway.com/api/charge", request, PaymentResponse.class);
            
            try {
                Thread.sleep(1000); // Simulate processing time
                return PaymentResponse.builder()
                    .orderId(request.getOrderId())
                    .status("APPROVED")
                    .transactionId("TXN-" + System.currentTimeMillis())
                    .message("Payment processed successfully")
                    .processedAt(LocalDateTime.now())
                    .build();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Payment processing interrupted", e);
            }
        });
    }
    
    // Fallback method when circuit breaker opens
    private CompletableFuture<PaymentResponse> paymentFallback(
            PaymentRequest request, 
            Exception ex) {
        log.warn("Circuit breaker active! Using alternative payment. Error: {}", 
                 ex.getMessage());
        
        // Here you can redirect to alternative gateway
        return CompletableFuture.completedFuture(
            PaymentResponse.builder()
                .orderId(request.getOrderId())
                .status("PENDING_RETRY")
                .message("Payment processing. You will receive confirmation shortly.")
                .processedAt(LocalDateTime.now())
                .build()
        );
    }
}
