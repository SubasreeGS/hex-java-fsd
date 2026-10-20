package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.SubmitInspectionReportDto;
import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDto;
import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDtoV2;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    //submit inspection report by inspector
    @PutMapping("/api/inspection/submit/{proposalId}")
    public void submitReport(@PathVariable Long proposalId,
                             @Valid @RequestBody SubmitInspectionReportDto dto) {
         inspectionService.submitReport(proposalId, dto);
    }

    //get report by inspector username (v1)
    @GetMapping("/api/inspection/inspector/v1")
    public List<InspectionResponseDto> getReportsByInspectorUsername(Principal principal,
                                                                     @RequestParam(name = "page",required = false,defaultValue = "0") int page,
                                                                     @RequestParam(name ="size",required = false,defaultValue = "10")int size)
    {
        return inspectionService.getReportsByInspectorUsername(principal.getName(),page,size);
    }

    //get report by inspector username and status (v2)
    @GetMapping("/api/inspection/status/v2/{status}")
    public List<InspectionResponseDto> getByStatus(@PathVariable InspectionStatus status,
                                                   Principal principal) {
        return inspectionService.getInspectionsByStatus(status,principal.getName());
    }


    //officer gets report by proposal id (v1)
    @GetMapping("/api/inspection/proposal/v1/{proposalId}")
    public InspectionResponseDtoV2 getByProposalId(@PathVariable Long proposalId) {
        return inspectionService.getByProposalId(proposalId);
    }

    //officer gets report by inspector id (v2)
    @GetMapping("/api/inspection/inspector/v2/{inspectorId}")
    public List<InspectionResponseDtoV2> getByInspectorId(@PathVariable Long inspectorId) {
        return inspectionService.getInspectionsByInspector(inspectorId);
    }


}
/*
Inspector - submit inspection report
inspector view report
    by username
    by username and status
officer view report
    by proposal id
    by inspectorid
 */