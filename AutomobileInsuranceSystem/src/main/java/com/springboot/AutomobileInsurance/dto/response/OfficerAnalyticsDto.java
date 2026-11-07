package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record OfficerAnalyticsDto(
        List<StatDto> claimsStats,
        List<StatDto> proposalStats,
        Double totalSettledPayout
) {
}
