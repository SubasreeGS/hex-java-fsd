package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.PolicyType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyResponseDto(
        Long policyId,
        String policyNumber,
        String customerName,
        String registrationNo,
        String vehicleMake,
        String vehicleModel,
        PolicyType policyType,
        BigDecimal totalPremium,
        LocalDate startDate,
        LocalDate endDate,
        String policyPdfUrl
) {}