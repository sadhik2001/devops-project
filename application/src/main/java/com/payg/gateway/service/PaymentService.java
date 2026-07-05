package com.payg.gateway.service;

import com.payg.gateway.model.PaymentRequest;
import com.payg.gateway.model.PaymentResponse;
import com.payg.gateway.model.Transaction;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PaymentService {

    private final TransactionRepository repository;
    private final Counter paymentSuccessCounter;
    private final Counter paymentFailureCounter;
    private final Timer paymentProcessingTimer;

    public PaymentService(TransactionRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.paymentSuccessCounter = Counter.builder("payg.payment.success")
            .description("Total successful payments processed")
            .register(meterRegistry);
        this.paymentFailureCounter = Counter.builder("payg.payment.failure")
            .description("Total failed payment attempts")
            .register(meterRegistry);
        this.paymentProcessingTimer = Timer.builder("payg.payment.processing.time")
            .description("Payment processing duration")
            .register(meterRegistry);
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        return paymentProcessingTimer.record(() -> {
            log.info("Processing payment for merchant: {}, amount: {} {}",
                request.getMerchantId(), request.getAmount(), request.getCurrency());

            // Idempotency check
            if (request.getReferenceId() != null) {
                var existing = repository.findByReferenceId(request.getReferenceId());
                if (existing.isPresent()) {
                    log.warn("Duplicate payment detected for referenceId: {}", request.getReferenceId());
                    return buildResponse(existing.get(), "Duplicate transaction – returning existing record");
                }
            }

            Transaction transaction = Transaction.builder()
                .merchantId(request.getMerchantId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .referenceId(request.getReferenceId() != null
                    ? request.getReferenceId() : UUID.randomUUID().toString())
                .status(Transaction.TransactionStatus.PROCESSING)
                .build();

            transaction = repository.save(transaction);

            // Simulate payment processing logic
            boolean success = simulatePaymentProcessor(request);

            transaction.setStatus(success
                ? Transaction.TransactionStatus.SUCCESS
                : Transaction.TransactionStatus.FAILED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transaction = repository.save(transaction);

            if (success) {
                paymentSuccessCounter.increment();
                log.info("Payment SUCCESS – transactionId: {}", transaction.getId());
            } else {
                paymentFailureCounter.increment();
                log.warn("Payment FAILED – transactionId: {}", transaction.getId());
            }

            return buildResponse(transaction, success ? "Payment processed successfully" : "Payment processing failed");
        });
    }

    public List<Transaction> getTransactionsByMerchant(String merchantId) {
        return repository.findByMerchantId(merchantId);
    }

    public Transaction getTransactionById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
    }

    private boolean simulatePaymentProcessor(PaymentRequest request) {
        // In production: integrate with payment processor (Stripe, Razorpay, etc.)
        return request.getAmount().doubleValue() < 100000.00;
    }

    private PaymentResponse buildResponse(Transaction tx, String message) {
        return PaymentResponse.builder()
            .transactionId(tx.getId())
            .status(tx.getStatus().name())
            .amount(tx.getAmount())
            .currency(tx.getCurrency())
            .referenceId(tx.getReferenceId())
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
