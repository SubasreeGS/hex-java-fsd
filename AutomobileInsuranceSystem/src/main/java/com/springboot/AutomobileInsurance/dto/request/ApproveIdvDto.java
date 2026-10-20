package com.springboot.AutomobileInsurance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ApproveIdvDto(
        @NotNull(message = "Approved IDV is required")
        BigDecimal approvedIdv
) {}