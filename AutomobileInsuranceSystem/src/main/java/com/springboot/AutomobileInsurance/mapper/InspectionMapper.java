package com.springboot.AutomobileInsurance.mapper;

import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDtoV2;
import com.springboot.AutomobileInsurance.model.Inspection;

public class InspectionMapper {

    public static InspectionResponseDtoV2 convertEntityToDto(Inspection inspection) {
        return new InspectionResponseDtoV2(
                inspection.getId(),
                inspection.getProposal().getId(),
                inspection.getProposal().getStatus(),
                inspection.getInspector().getId(),
                inspection.getInspector().getName(),
                inspection.getResult(),
                inspection.getRecommendedIdv(),
                inspection.getApprovedIdv(),
                inspection.getInspectionPhotosUrl(),
                inspection.getInspectionNotes(),
                inspection.getStatus()
        );
    }
}