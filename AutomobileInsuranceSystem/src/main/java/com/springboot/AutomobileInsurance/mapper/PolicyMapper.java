package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto;
import com.springboot.AutomobileInsurance.model.Policy;

public class PolicyMapper {

    public static PolicyResponseDto convertEntityToDto(Policy policy) {
        return new PolicyResponseDto(
                policy.getId(),
                policy.getPolicyNumber(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getTotalPremium(),
                policy.getPolicyPdfUrl(),
                policy.getStatus()
        );
    }


}