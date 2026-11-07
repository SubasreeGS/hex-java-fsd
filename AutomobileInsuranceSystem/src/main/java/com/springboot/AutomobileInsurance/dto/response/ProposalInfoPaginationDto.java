package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record ProposalInfoPaginationDto(
        List<ProposalInfoDtoV2> list,
        PaginationDto paginationInfo
) {
}
