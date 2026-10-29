package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.response.ClaimAssessmentResponseDto;
import com.springboot.AutomobileInsurance.model.ClaimAssessment;

public final class ClaimAssessmentMapper {


    public static ClaimAssessmentResponseDto convertEntityToDto(ClaimAssessment assessment) {

        return new ClaimAssessmentResponseDto(

               assessment.getClaim().getClaimNo(),
                assessment.getInspector().getName(),
                assessment.getEstimatedCost(),
                assessment.getDamagePhotosUrl(),
                assessment.getAssessmentNotes(),
                assessment.getStatus()
        );
    }
}