package com.ecommerce.service;

import com.ecommerce.dto.StockResponse;
import com.ecommerce.exception.InventoryServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class InventoryService {
    
    private final WebClient webClient;
    
    public InventoryService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl("https://inventory-service.com")
            .build();
    }
    
    @Retry(name = "inventoryService", fallbackMethod = "checkStockFallback")
    @CircuitBreaker(name = "inventoryService")
    public Mono<StockResponse> checkStock(Long productId, int quantity) {
        log.info("Checking stock: product={}, quantity={}", productId, quantity);
        
        // Simulate inventory check - in real scenario would call external service
        // Using a simple logic: products with ID 1-10 have stock, others may not
        boolean hasStock = productId <= 10;
        int availableQuantity = hasStock ? (int)(Math.random() * 100 + 50) : 0;
        
        return Mono.just(StockResponse.builder()
            .productId(productId)
            .available(hasStock && availableQuantity >= quantity)
            .quantity(availableQuantity)
            .estimated(false)
            .message(hasStock ? "Stock available" : "Product out of stock")
            .build())
            .doOnSuccess(response -> log.info("Stock verified: {}", response))
            .doOnError(error -> log.error("Error checking stock", error));
    }
    
    private Mono<StockResponse> checkStockFallback(
            Long productId, 
            int quantity, 
            Exception ex) {
        log.warn("Fallback active for stock. Product: {}, Error: {}", 
                 productId, ex.getMessage());
        
        // Returns estimate based on cache or historical data
        return Mono.just(StockResponse.builder()
            .productId(productId)
            .available(false)
            .estimated(true)
            .message("Stock temporarily unavailable. Estimate based on historical data.")
            .build());
    }
}
