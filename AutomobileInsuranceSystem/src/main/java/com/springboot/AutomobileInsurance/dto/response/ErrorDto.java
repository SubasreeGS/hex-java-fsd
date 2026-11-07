package com.springboot.AutomobileInsurance.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;

public record ErrorDto(
        String message,
        String comments,
        Instant timestamp
) {}