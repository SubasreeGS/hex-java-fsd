package com.springboot.AutomobileInsurance.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InspectorUpdateDto(
        @NotBlank(message = "Name is required")
        @NotNull(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @NotNull(message = "Email is required")
        @Email(message = "Email seems invalid")
        String email,

        @NotBlank(message = "Contact is required")
        @NotNull(message = "Contact is required")
        @Size(min = 10, max = 10, message = "Contact should be a 10-digit mobile number")
        String contact

) {
}
