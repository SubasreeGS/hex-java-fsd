package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.AssessmentStatus;

import java.math.BigDecimal;

public record ClaimAssessmentResponseDto(

        String claimNo,
        String inspectorName,
        BigDecimal estimatedCost,
        String damagePhotosUrl,
        String assessmentNotes,
        AssessmentStatus status
) {}