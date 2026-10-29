package com.springboot.AutomobileInsurance.dto.request;

import com.springboot.AutomobileInsurance.enums.AssessmentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ClaimAssessmentRequestDto(
        @NotNull(message = "Claim ID is mandatory")
        @NotBlank(message = "Claim ID is mandatory")
        Long claimId,

        @NotNull(message = "Inspector ID is mandatory")
        @NotBlank(message = "Inspector ID is mandatory")
        Long inspectorId,

        @NotNull(message = "Estimated cost is mandatory")
        BigDecimal estimatedCost,

        String damagePhotosUrl,
        String assessmentNotes,
        AssessmentStatus status
) {}