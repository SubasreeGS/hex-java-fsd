package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.ClaimStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ClaimRespDto(
        Long id,
        String claimNo,
        String customerName,
        BigDecimal claimedAmount,
        ClaimStatus status,
        Instant createdAt
) {
}
