package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InspectorClaimRespDto(
        Long id,
        String claimNo,
        String customerName,
        String vehicleNumber,
        BigDecimal claimedAmount,
        ClaimStatus status,
        LocalDate incidentDate
) {
}
