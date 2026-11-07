package com.springboot.AutomobileInsurance.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record InspectorUpdateDto(
        String name,
        @Email(message = "Invalid email format")
        String email,

        @Size(min = 10, max = 10, message = "Contact must be a 10-digit mobile number")
        String contact
) {}