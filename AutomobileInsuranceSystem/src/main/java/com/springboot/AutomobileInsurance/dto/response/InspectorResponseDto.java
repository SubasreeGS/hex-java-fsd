package com.springboot.AutomobileInsurance.dto.response;

public record InspectorResponseDto(
        Long id,
        String name,
        Long userId,
        String username
) {}