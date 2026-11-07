package com.springboot.AutomobileInsurance.dto.response;

import com.springboot.AutomobileInsurance.enums.Role;

public record DetailsDto(
        String username,
        Boolean isActive,
        Role role
) {
}
