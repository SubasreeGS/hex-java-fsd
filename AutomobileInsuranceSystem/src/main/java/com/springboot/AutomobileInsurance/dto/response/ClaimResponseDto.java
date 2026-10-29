package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.ClaimStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClaimResponseDto(

        String claimNo,
        String policyNumber,
        LocalDate incidentDate,
        String incidentDescription,
        BigDecimal claimedAmount,
        BigDecimal approvedAmount,
        ClaimStatus status,
        String officerName,
        Instant createdAt,
        Instant updatedAt
) {}