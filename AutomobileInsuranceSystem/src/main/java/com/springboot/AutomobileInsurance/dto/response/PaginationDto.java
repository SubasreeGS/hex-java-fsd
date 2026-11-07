package com.springboot.AutomobileInsurance.dto.response;

public record PaginationDto(
        int totalPages,
        long totalRecords,
        int numRecordsInPage
) {
}
