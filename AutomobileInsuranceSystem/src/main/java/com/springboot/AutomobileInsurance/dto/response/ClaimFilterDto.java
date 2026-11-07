package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record ClaimFilterDto(
        List<ClaimRespDto> list,
        PaginationDto paginationInfo
) {
}
