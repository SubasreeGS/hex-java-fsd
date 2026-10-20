package com.springboot.AutomobileInsurance.dto.request;

import com.springboot.AutomobileInsurance.enums.PolicyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProposalSubmissionDto(

        @NotBlank(message = "Vehicle registration number is required")
        @NotNull(message = "Vehicle registration number is required")
        String registrationNo,

        @NotBlank(message = "RC document URL is required")
        @NotNull(message = "RC document URL is required")
        String rcDocUrl,

        @NotNull(message = "Policy type is required")
        PolicyType policyType,

        boolean isZeroDep,
        boolean isEngineProtect,
        boolean isRoadsideAssist
) {}