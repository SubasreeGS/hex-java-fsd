package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;

public record ProposalResponseDto(
        Long id,
        Long customerId,
        String customerName,
        Long vehicleId,
        String registrationNo,
        String vehicleMake,
        String vehicleModel,
        int manufactureYear,
        Long officerId,
        String officerName,
        String rcDocUrl,
        PolicyType policyType,
        boolean isZeroDep,
        boolean isEngineProtect,
        boolean isRoadsideAssist,
        ProposalStatus status
) {}