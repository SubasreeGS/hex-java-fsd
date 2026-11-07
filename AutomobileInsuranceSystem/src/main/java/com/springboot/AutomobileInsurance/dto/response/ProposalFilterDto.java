package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record ProposalFilterDto(
        List<ProposalRespDto> list,
        PaginationDto paginationInfo
) {
}
