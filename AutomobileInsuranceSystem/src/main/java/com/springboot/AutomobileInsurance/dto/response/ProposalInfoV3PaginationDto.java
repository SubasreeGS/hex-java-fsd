package com.springboot.AutomobileInsurance.dto.response;

import java.util.List;

public record ProposalInfoV3PaginationDto(
        List<ProposalInfoDtoV3> list,
        PaginationDto paginationInfo
) {
}
