package com.payg.gateway.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private String transactionId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String referenceId;
    private String message;
    private LocalDateTime timestamp;
}
