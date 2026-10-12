package com.springboot.AutomobileInsurance.dto.response;

public record OfficerResponseDto(
        Long id,
        String name,
        Long userId,
        String username
) {}