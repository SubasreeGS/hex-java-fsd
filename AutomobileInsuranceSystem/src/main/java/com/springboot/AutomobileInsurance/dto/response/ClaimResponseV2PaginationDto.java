package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record ClaimResponseV2PaginationDto(
        List<ClaimResponseDtoV2> list,
        PaginationDto paginationInfo
) {
}
