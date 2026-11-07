package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record FilterDto(
        List<LedgerRespDto> list,
        PaginationDto paginationInfo
) {
}
