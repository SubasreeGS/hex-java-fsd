package com.springboot.AutomobileInsurance.dto.response;

public record AdminStatsDto(
        long registeredOfficers,
        long activeInspectors,
        long totalCustomers,
        long issuedPolicies
) {
}
