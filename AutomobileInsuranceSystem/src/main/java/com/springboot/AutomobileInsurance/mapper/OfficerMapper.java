package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.request.OfficerDto;
import com.springboot.AutomobileInsurance.dto.response.OfficerResponseDto;
import com.springboot.AutomobileInsurance.model.Officer;

public class OfficerMapper {

    public static Officer convertDtoToEntity(OfficerDto dto) {
        Officer officer = new Officer();
        officer.setName(dto.name());
        return officer;
    }

    public static OfficerResponseDto convertEntityToDto(Officer officer) {
        return new OfficerResponseDto(
                officer.getId(),
                officer.getName(),
               officer.getUser().getId(),
                officer.getUser().getUsername()
        );
    }
}