package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long orderId;
    private String status; // APPROVED, PENDING_RETRY, DECLINED
    private String transactionId;
    private String message;
    private LocalDateTime processedAt;
}
