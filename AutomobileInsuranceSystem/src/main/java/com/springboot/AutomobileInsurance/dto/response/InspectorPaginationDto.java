package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record InspectorPaginationDto(
        List<InspectorResponseDto> list,
        PaginationDto paginationInfo
) {
}
