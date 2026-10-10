package com.springboot.AutomobileInsurance.dto.response;

import java.time.LocalDateTime;

public record ErrorDto(
        String message,
        String status,
        LocalDateTime timestamp
) {}