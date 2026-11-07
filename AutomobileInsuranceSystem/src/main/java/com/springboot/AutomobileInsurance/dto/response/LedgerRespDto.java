package com.springboot.AutomobileInsurance.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record LedgerRespDto(
        Long id,
        String referenceNo,
        String customerName,
        String vehicleNumber,
        BigDecimal amount,
        String status,
        String date
) {
}
