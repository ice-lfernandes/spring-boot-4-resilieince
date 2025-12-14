package com.ecommerce.controller;

import com.ecommerce.dto.Product;
import com.ecommerce.service.ProductService;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping("/search")
    @RateLimiter(name = "productSearch")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page) {
        
        log.info("Searching products: {} - page {}", query, page);
        List<Product> products = productService.search(query, page);
        
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    @RateLimiter(name = "productDetail", fallbackMethod = "productDetailFallback")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("Fetching product: {}", id);
        Product product = productService.findById(id);
        return ResponseEntity.ok(product);
    }
    
    private ResponseEntity<Product> productDetailFallback(
            Long id, 
            RequestNotPermitted ex) {
        log.warn("Rate limit exceeded for product: {}", id);
        
        // Returns cached or simplified data
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .header("X-RateLimit-Retry-After", "60")
            .body(productService.getCachedProduct(id));
    }
}
