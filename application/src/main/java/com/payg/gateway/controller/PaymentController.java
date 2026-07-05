package com.payg.gateway.controller;

import com.payg.gateway.model.PaymentRequest;
import com.payg.gateway.model.PaymentResponse;
import com.payg.gateway.model.Transaction;
import com.payg.gateway.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request for merchant: {}", request.getMerchantId());
        PaymentResponse response = paymentService.processPayment(request);
        HttpStatus status = "SUCCESS".equals(response.getStatus()) ? HttpStatus.CREATED : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getTransactionById(transactionId));
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<Transaction>> getMerchantTransactions(@PathVariable String merchantId) {
        return ResponseEntity.ok(paymentService.getTransactionsByMerchant(merchantId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "PayG Plus Payment Gateway",
            "version", "1.0.0"
        ));
    }
}
