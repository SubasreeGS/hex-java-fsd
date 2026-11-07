package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClaimSettlementReceiptDto(
        Long claimId,
        String claimNo,
        String policyNumber,
        String customerName,
        String customerPhone,
        String registrationNo,
        String originalPaymentTxnRef,
        BigDecimal claimedAmount,
        BigDecimal approvedAmount,
        String settlementRef,
        LocalDateTime settledDate,
        ClaimStatus status,
        String officerUsername
) {
}
