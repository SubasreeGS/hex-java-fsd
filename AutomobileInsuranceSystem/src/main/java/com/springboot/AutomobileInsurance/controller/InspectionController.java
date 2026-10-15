package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.SubmitInspectionReportDto;
import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDto;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    @PutMapping("/api/inspection/submit/{proposalId}")
    public InspectionResponseDto submitReport(@PathVariable Long proposalId,
                                              @Valid @RequestBody SubmitInspectionReportDto dto) {
        return inspectionService.submitReport(proposalId, dto);
    }

    @GetMapping("/api/inspection/proposal/{proposalId}")
    public InspectionResponseDto getByProposalId(@PathVariable Long proposalId) {
        return inspectionService.getByProposalId(proposalId);
    }

    @GetMapping("/api/inspection/inspector/{inspectorId}")
    public List<InspectionResponseDto> getByInspectorId(@PathVariable Long inspectorId) {
        return inspectionService.getInspectionsByInspector(inspectorId);
    }

    @GetMapping("/api/inspection/status/{status}")
    public List<InspectionResponseDto> getByStatus(@PathVariable InspectionStatus status) {
        return inspectionService.getInspectionsByStatus(status);
    }
}