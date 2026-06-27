package com.payg.gateway.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotBlank
    private String paymentMethod;

    private String referenceId;
}
