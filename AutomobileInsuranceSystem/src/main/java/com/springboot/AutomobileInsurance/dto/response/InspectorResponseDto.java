package com.springboot.AutomobileInsurance.dto.response;

public record InspectorResponseDto(
        Long id,
        String name,
        String username,
        String email,
        String contact,
        String officerUsername
) {}