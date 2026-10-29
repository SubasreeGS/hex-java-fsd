package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.response.ClaimResponseDto;
import com.springboot.AutomobileInsurance.model.Claim;

public final class ClaimMapper {


    public static ClaimResponseDto convertEntityToDto(Claim claim) {

        return new ClaimResponseDto(

                claim.getClaimNo(),
                claim.getPolicy().getPolicyNumber(),
                claim.getIncidentDate(),
                claim.getIncidentDescription(),
                claim.getClaimedAmount(),
                claim.getApprovedAmount(),
                claim.getStatus(),
                claim.getOfficer().getName(),
                claim.getCreatedAt(),
                claim.getUpdatedAt()
        );
    }
}