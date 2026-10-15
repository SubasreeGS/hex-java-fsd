package com.springboot.AutomobileInsurance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ApproveIdvDto(
        @NotNull(message = "Approved IDV is required")
        @DecimalMin(value = "1000.00", message = "Approved IDV must be at least 1000")
        BigDecimal approvedIdv
) {}