package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.request.InspectorDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorResponseDto;
import com.springboot.AutomobileInsurance.model.Inspector;

public class InspectorMapper {

    public static Inspector convertDtoToEntity(InspectorDto dto) {
        Inspector inspector = new Inspector();
        inspector.setName(dto.name());
        return inspector;
    }

    public static InspectorResponseDto convertEntityToDto(Inspector inspector) {
        return new InspectorResponseDto(
                inspector.getId(),
                inspector.getName(),
                inspector.getUser().getId(),
                inspector.getUser().getUsername()
        );
    }
}