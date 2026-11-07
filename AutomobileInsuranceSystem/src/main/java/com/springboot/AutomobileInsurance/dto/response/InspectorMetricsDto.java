package com.springboot.AutomobileInsurance.dto.response;

public record InspectorMetricsDto(
        long pendingVehicleSurveys,
        long pendingClaimSurveys,
        long completedSurveys
) {
}
