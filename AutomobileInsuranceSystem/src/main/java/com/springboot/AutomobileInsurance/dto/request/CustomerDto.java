package com.springboot.AutomobileInsurance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerDto(
        @NotBlank(message = "Username is required")
        @NotNull(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @NotNull(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
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
        @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN must be a valid 10-character code (e.g. ABCDE1234F)")
        String panNo
) {}