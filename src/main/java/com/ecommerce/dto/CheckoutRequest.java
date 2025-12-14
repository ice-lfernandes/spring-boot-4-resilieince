package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private String cartId;
    private Long customerId;
    private Long productId;
    private int quantity;
    private BigDecimal totalAmount;
    private String paymentMethod;
}
