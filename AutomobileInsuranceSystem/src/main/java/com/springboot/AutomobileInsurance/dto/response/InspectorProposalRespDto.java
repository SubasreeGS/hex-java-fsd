package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;

import java.time.Instant;

public record InspectorProposalRespDto(
        Long inspectionId,
        Long proposalId,
        String customerName,
        String vehicleNumber,
        PolicyType policyType,
        InspectionStatus inspectionStatus,
        Instant submittedOn
) {
}
