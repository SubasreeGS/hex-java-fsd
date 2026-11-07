package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.PolicyStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyResponseDtoV2(
        Long id,
        String policyNumber,
        String customerName,
        String registrationNo,
        String vehicleMake,
        String vehicleModel,
        PolicyType policyType,
        BigDecimal idv,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalPremium,
        PolicyStatus status,
        String policyPdfUrl
) {
}
