package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.ClaimStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ClaimResponseDtoV2(

        Long id,
        String claimNo,
        String policyNumber,
        LocalDate incidentDate,
        String incidentDescription,
        BigDecimal claimedAmount,
        BigDecimal approvedAmount,
        ClaimStatus status,
        String officerName,
        String inspectorName,
        Instant createdAt,
        Instant updatedAt
) {
}
