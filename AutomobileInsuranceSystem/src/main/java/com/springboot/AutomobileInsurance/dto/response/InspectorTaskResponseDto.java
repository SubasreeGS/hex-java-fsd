package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.AssessmentStatus;
import com.springboot.AutomobileInsurance.enums.FuelType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InspectorTaskResponseDto(
        // 1. Task & Claim Reference
        Long assessmentId,
        String claimNo,

        // 2. Vehicle Verification Specs
        String registrationNo,
        String vehicleMake,
        String vehicleModel,
        Integer manufactureYear,
        String vinNo,
        FuelType fuelType,

        // 3. Customer Contact & Location
        String customerName,
        String customerPhone,
        String customerAddress,

        // 4. Incident & Claim Specifics
        LocalDate incidentDate,
        String incidentDescription,
        BigDecimal claimedAmount,

        // 5. Workflow Status
        AssessmentStatus assessmentStatus
) {
}
