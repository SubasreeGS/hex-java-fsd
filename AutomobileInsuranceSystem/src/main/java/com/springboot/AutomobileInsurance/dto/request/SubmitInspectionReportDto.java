package com.springboot.AutomobileInsurance.dto.request;

import com.springboot.AutomobileInsurance.enums.InspectionResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SubmitInspectionReportDto(
        @NotNull(message = "Inspection result (PASSED/FAILED) is required")
        InspectionResult result,

        BigDecimal recommendedIdv,

        @NotBlank(message = "Inspection photos URL is required")
        String inspectionPhotosUrl,

        String inspectionNotes
) {}