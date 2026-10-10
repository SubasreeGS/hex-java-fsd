package com.springboot.AutomobileInsurance.dto.response;

import java.time.LocalDate;

public record CustomerResponseDto(
        Long id,
        String name,
        String phoneNo,
        String address,
        LocalDate dob,
        String aadhaarNo,
        String panNo,
        Long userId,
        String username
) {}