package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record OfficerPaginationDto(
        List<OfficerResponseDto> list,
        PaginationDto paginationInfo
) {
}
