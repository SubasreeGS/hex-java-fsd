package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.request.InspectorDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorResponseDto;
import com.springboot.AutomobileInsurance.model.Inspector;

public class InspectorMapper {

    public static Inspector convertDtoToEntity(InspectorDto dto) {
        Inspector inspector = new Inspector();
        inspector.setName(dto.name());
        inspector.setEmail(dto.email());
        inspector.setContact(dto.contact());
        return inspector;
    }

    public static InspectorResponseDto convertEntityToDto(Inspector ins) {
        return new InspectorResponseDto(
                ins.getId(),
                ins.getName(),
                ins.getUser().getUsername(),
                ins.getEmail(),
                ins.getContact(),
                ins.getOfficer().getUser().getUsername()
        );

    }

}