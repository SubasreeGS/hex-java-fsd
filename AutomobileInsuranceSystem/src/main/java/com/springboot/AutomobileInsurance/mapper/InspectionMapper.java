package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDto;
import com.springboot.AutomobileInsurance.model.Inspection;

public class InspectionMapper {

    public static InspectionResponseDto convertEntityToDto(Inspection inspection) {
        return new InspectionResponseDto(
                inspection.getId(),
                inspection.getProposal().getId(),
                inspection.getProposal().getStatus(),
                inspection.getInspector().getId(),
                inspection.getInspector().getName(),
                inspection.getResult(),
                inspection.getRecommendedIdv(),
                inspection.getInspectionPhotosUrl(),
                inspection.getInspectionNotes(),
                inspection.getStatus()
        );
    }
}