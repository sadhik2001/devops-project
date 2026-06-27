package com.payg.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payg.gateway.model.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/v1/payments/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void processPaymentReturnsCreatedForValidRequest() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setMerchantId("MERCH-001");
        request.setAmount(new BigDecimal("500.00"));
        request.setCurrency("INR");
        request.setPaymentMethod("UPI");
        request.setReferenceId("REF-TEST-001");

        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.transactionId").isNotEmpty());
    }

    @Test
    void processPaymentReturnsBadRequestForMissingFields() throws Exception {
        PaymentRequest request = new PaymentRequest();
        // Missing required fields

        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}
