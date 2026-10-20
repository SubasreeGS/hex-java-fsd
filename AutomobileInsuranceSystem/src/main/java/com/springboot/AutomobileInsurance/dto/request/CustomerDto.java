package com.springboot.AutomobileInsurance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerDto(
        @NotBlank(message = "Username is required")
        @NotNull(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        @NotNull(message = "Password is required")
        String password,

        @NotBlank(message = "Name is required")
        @NotNull(message = "Name is required")
        String name,

        @NotBlank(message = "Phone number is required")
        @NotNull(message = "Phone number is required")
        @Size(min = 10, max = 10, message = "Phone number should be a 10 digit mobile number")
        String phoneNo,

        @NotBlank(message = "Address is required")
        @NotNull(message = "Address is required")
        String address,

        @NotNull(message = "Date of birth is required")
        LocalDate dob,

        @NotBlank(message = "Aadhaar number is required")
        @NotNull(message = "Aadhaar number is required")
        @Size(min = 12, max = 12, message = "Aadhaar number must be exactly 12 digits")
        String aadhaarNo,

        @NotBlank(message = "PAN number is required")
        @NotNull(message = "PAN number is required")
        String panNo
) {}