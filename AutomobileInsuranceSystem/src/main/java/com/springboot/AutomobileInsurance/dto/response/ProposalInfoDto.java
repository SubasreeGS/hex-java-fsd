package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;

public record ProposalInfoDto(
        Long id,
        ProposalStatus status,
        PolicyType policyType,
        boolean isZeroDep,
        boolean isEngineProtect,
        boolean isRoadsideAssist,
        String inspectorName,
        String inspectorEmail,
        String inspectorContact,
        String registrationNo,
        String make,
        String model
) {
}
