package com.springboot.AutomobileInsurance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OfficerDto(
        @NotBlank(message = "Username is required")
        @NotNull(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        @NotNull(message = "Password is required")
        String password,

        @NotBlank(message = "Name is required")
        @NotNull(message = "Name is required")
        String name
) {}