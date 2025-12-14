package com.ecommerce.service;

import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.dto.CheckoutResponse;
import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.entity.Order;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckoutService {
    
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    
    @Transactional
    public CompletableFuture<CheckoutResponse> processCheckout(CheckoutRequest request) {
        log.info("Starting checkout for cart: {}", request.getCartId());
        
        // 1. Validates stock with retry and circuit breaker
        return inventoryService.checkStock(request.getProductId(), request.getQuantity())
            .flatMap(stockResponse -> {
                if (!stockResponse.isAvailable()) {
                    return Mono.error(new InsufficientStockException(
                        "Product out of stock"));
                }
                
                // 2. Creates the order
                Order order = createOrder(request);
                Order savedOrder = orderRepository.save(order);
                
                // 3. Processes payment with circuit breaker and timeout
                return Mono.fromFuture(
                    paymentService.processPayment(toPaymentRequest(savedOrder))
                ).map(paymentResponse -> {
                    savedOrder.setPaymentStatus(paymentResponse.getStatus());
                    orderRepository.save(savedOrder);
                    return savedOrder;
                });
            })
            .doOnSuccess(order -> {
                // 4. Sends notifications asynchronously (non-blocking)
                // Bulkhead ensures this doesn't consume all resources
                notificationService.sendEmailNotification(order);
                notificationService.sendSmsNotification(order);
            })
            .map(order -> CheckoutResponse.builder()
                .orderId(order.getId())
                .status(order.getPaymentStatus())
                .message("Order processed successfully!")
                .timestamp(LocalDateTime.now())
                .build())
            .doOnError(error -> log.error("Checkout error", error))
            .toFuture();
    }
    
    private Order createOrder(CheckoutRequest request) {
        return Order.builder()
            .customerId(request.getCustomerId())
            .productId(request.getProductId())
            .quantity(request.getQuantity())
            .totalAmount(request.getTotalAmount())
            .status("PENDING")
            .customerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail() : "customer@example.com")
            .customerPhone(request.getCustomerPhone() != null ? request.getCustomerPhone() : "+1234567890")
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    private PaymentRequest toPaymentRequest(Order order) {
        return PaymentRequest.builder()
            .orderId(order.getId())
            .amount(order.getTotalAmount())
            .customerId(order.getCustomerId())
            .build();
    }
}
