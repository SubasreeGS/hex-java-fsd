package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentReceiptDto(
        Long paymentId,
        String transactionRef,
        Long quoteId,
        Long proposalId,
        String policyNumber,
        String customerName,
        String registrationNo,
        BigDecimal amountPaid,
        LocalDateTime paymentDate,
        PaymentStatus paymentStatus
) {}