package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.InspectionResult;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;

import java.math.BigDecimal;

public record InspectionResponseDtoV2(
        Long inspectionId,
        Long proposalId,
        ProposalStatus proposalStatus,
        Long inspectorId,
        String inspectorName,
        InspectionResult result,
        BigDecimal recommendedIdv,
        BigDecimal approvedIdv,
        String inspectionPhotosUrl,
        String inspectionNotes,
        InspectionStatus inspectionStatus
) {}