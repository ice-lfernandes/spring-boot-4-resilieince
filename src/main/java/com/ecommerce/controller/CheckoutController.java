package com.ecommerce.controller;

import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.dto.CheckoutResponse;
import com.ecommerce.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/checkout")
@Slf4j
@RequiredArgsConstructor
public class CheckoutController {
    
    private final CheckoutService checkoutService;
    
    @PostMapping
    public CompletableFuture<ResponseEntity<CheckoutResponse>> processCheckout(
            @RequestBody CheckoutRequest request) {
        
        log.info("Checkout request received: {}", request);
        
        return checkoutService.processCheckout(request)
            .thenApply(ResponseEntity::ok)
            .exceptionally(ex -> {
                log.error("Checkout failed", ex);
                return ResponseEntity.internalServerError().build();
            });
    }
}
