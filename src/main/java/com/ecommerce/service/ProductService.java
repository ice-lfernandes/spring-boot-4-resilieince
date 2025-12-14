package com.ecommerce.service;

import com.ecommerce.dto.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService {
    
    private final Map<Long, Product> productCache = new HashMap<>();
    
    public ProductService() {
        // Initialize with some sample products
        productCache.put(1L, Product.builder()
            .id(1L)
            .name("Laptop")
            .description("High-performance laptop")
            .price(new BigDecimal("1299.99"))
            .stockQuantity(50)
            .build());
        
        productCache.put(2L, Product.builder()
            .id(2L)
            .name("Smartphone")
            .description("Latest smartphone model")
            .price(new BigDecimal("799.99"))
            .stockQuantity(100)
            .build());
        
        productCache.put(3L, Product.builder()
            .id(3L)
            .name("Tablet")
            .description("Portable tablet device")
            .price(new BigDecimal("499.99"))
            .stockQuantity(75)
            .build());
    }
    
    public List<Product> search(String query, int page) {
        log.debug("Searching products with query: {}", query);
        
        return productCache.values().stream()
            .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    public Product findById(Long id) {
        log.debug("Finding product by id: {}", id);
        return productCache.get(id);
    }
    
    public Product getCachedProduct(Long id) {
        log.debug("Getting cached product: {}", id);
        Product product = productCache.get(id);
        if (product != null) {
            return product;
        }
        
        // Return minimal product info if not in cache
        return Product.builder()
            .id(id)
            .name("Product " + id)
            .description("Product details temporarily unavailable")
            .price(BigDecimal.ZERO)
            .stockQuantity(0)
            .build();
    }
}
