package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record InspectorProposalFilterDto(
        List<InspectorProposalRespDto> list,
        PaginationDto paginationInfo
) {
}
