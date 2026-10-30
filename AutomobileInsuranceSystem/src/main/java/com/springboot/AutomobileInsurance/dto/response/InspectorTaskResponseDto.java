package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.AssessmentStatus;
import com.springboot.AutomobileInsurance.enums.FuelType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InspectorTaskResponseDto(
        // 1. Assessment & Claim
        Long assessmentId,
        String claimNo,

        // 2. RTO Vehicle Specifications (from RtoVehicle)
        String registrationNo,
        String make,
        String model,
        Integer manufactureYear,
        String vinNo,
        FuelType fuelType,

        // 3. Customer Contact Info (from Customer)
        String customerName,
        String customerPhone,
        String customerAddress,

        // 4. Incident Specifics (from Claim)
        LocalDate incidentDate,
        String incidentDescription,
        BigDecimal claimedAmount,

        // 5. Assessment Status (from ClaimAssessment)
        AssessmentStatus assessmentStatus
) {
}
