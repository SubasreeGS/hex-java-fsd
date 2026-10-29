package com.springboot.AutomobileInsurance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClaimFileRequestDto(
        @NotNull(message = "Policy ID is mandatory")
        Long policyId,

        @NotNull(message = "Incident date is mandatory")
        LocalDate incidentDate,

        @NotBlank(message = "Incident description cannot be blank")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String incidentDescription,

        @NotNull(message = "Claimed amount is mandatory")
        @DecimalMin(value = "1.00", message = "Claimed amount must be at least ₹1.00")
        BigDecimal claimedAmount
) {}