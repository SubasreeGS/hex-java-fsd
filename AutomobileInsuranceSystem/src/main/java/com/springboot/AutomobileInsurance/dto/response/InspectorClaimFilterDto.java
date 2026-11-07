package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record InspectorClaimFilterDto(
        List<InspectorClaimRespDto> list,
        PaginationDto paginationInfo
) {
}
