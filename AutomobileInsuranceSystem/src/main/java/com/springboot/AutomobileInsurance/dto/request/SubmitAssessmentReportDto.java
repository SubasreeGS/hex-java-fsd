package com.springboot.AutomobileInsurance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SubmitAssessmentReportDto(
        @NotNull(message = "Estimated repair cost is mandatory")
        BigDecimal estimatedCost,

        @NotBlank(message = "Damage photos URL is mandatory")
        @Size(max = 500, message = "URL cannot exceed 500 characters")
        String damagePhotosUrl,

        @NotBlank(message = "Assessment notes are mandatory")
        String assessmentNotes
) {
}
