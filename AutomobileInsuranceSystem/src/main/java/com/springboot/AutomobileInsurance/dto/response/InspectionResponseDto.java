package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.InspectionResult;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;

import java.math.BigDecimal;

public record InspectionResponseDto(
        Long proposalId,
        InspectionResult result,
        BigDecimal recommendedIdv,
        String inspectionPhotosUrl,
        String inspectionNotes,
        InspectionStatus status

) {
}
