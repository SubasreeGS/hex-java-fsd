package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record InspectorTaskPaginationDto(
        List<InspectorTaskResponseDto> list,
        PaginationDto paginationInfo
) {
}
