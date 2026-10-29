package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.PolicyType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyResponseDtoV2(

        String policyNumber,

        String customerName,

        String registrationNo,
        String vehicleMake,
        String vehicleModel,

        PolicyType policyType,

        LocalDate startDate,
        LocalDate endDate,

        BigDecimal totalPremium,
        String policyPdfUrl
) {
}
