package com.springboot.AutomobileInsurance.dto.request;

import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ClaimReviewRequestDto(
        @NotNull(message = "Officer ID is mandatory")
        Long officerId,

        @NotNull(message = "Status is mandatory (APPROVED or REJECTED)")
        ClaimStatus status,


        BigDecimal approvedAmount
) {}