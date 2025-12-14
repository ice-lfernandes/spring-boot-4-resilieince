package com.ecommerce.service;

import com.ecommerce.entity.Order;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    
    @Bulkhead(name = "emailNotification", type = Bulkhead.Type.THREADPOOL)
    @Async
    public CompletableFuture<Void> sendEmailNotification(Order order) {
        log.info("Sending email for order: {}", order.getId());
        
        try {
            // Simulate email sending
            Thread.sleep(500);
            log.info("Email sent successfully: {}", order.getId());
            
        } catch (Exception e) {
            log.error("Error sending email: {}", order.getId(), e);
            // Does not propagate error - notification is non-critical
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Bulkhead(name = "smsNotification", type = Bulkhead.Type.SEMAPHORE)
    @RateLimiter(name = "smsNotification")
    public void sendSmsNotification(Order order) {
        log.info("Sending SMS for order: {}", order.getId());
        
        try {
            // Simulate SMS sending
            Thread.sleep(300);
            log.info("SMS sent successfully: {}", order.getId());
        } catch (Exception e) {
            log.error("Error sending SMS: {}", order.getId(), e);
        }
    }
}
