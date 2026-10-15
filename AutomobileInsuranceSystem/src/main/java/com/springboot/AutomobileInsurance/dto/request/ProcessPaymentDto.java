package com.springboot.AutomobileInsurance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProcessPaymentDto(
        @NotBlank(message = "Transaction reference is required")
        String transactionRef,

        @NotNull(message = "Payment amount is required")
        BigDecimal amount,

        @NotBlank(message = "Payment mode (UPI, CARD, NETBANKING) is required")
        String paymentMode
) {}