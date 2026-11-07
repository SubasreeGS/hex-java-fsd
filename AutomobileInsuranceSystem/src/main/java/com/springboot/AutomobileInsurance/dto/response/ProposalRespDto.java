package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;

import java.time.Instant;

public record ProposalRespDto(
        Long id,
        String customerName,
        String vehicleNumber,
        PolicyType policyType,
        ProposalStatus status,
        Instant createdAt
) {
}
