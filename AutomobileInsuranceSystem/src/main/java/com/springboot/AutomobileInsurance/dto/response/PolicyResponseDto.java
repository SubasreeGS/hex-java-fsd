package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.PolicyStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyResponseDto(
        Long id,
        String policyNumber,
        String customerName,
        String registrationNo,
        String vehicleMake,
        String vehicleModel,
        PolicyType policyType,
        BigDecimal idv,
        BigDecimal totalPremium,
        LocalDate startDate,
        LocalDate endDate,
        PolicyStatus status,
        String policyPdfUrl
) {}