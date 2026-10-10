package com.springboot.AutomobileInsurance.dto.request;

import java.time.LocalDate;

public record CustomerUpdateDto(
        String name,
        String phoneNo,
        String address,
        LocalDate dob
) {}