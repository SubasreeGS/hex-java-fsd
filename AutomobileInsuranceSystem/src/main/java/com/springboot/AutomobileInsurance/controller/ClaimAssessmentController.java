package com.springboot.AutomobileInsurance.controller;

import com.springboot.AutomobileInsurance.dto.request.ClaimAssessmentRequestDto;
import com.springboot.AutomobileInsurance.dto.request.SubmitAssessmentReportDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimAssessmentResponseDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorTaskPaginationDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorTaskResponseDto;
import com.springboot.AutomobileInsurance.service.ClaimAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")

public class ClaimAssessmentController {

    private final ClaimAssessmentService assessmentService;

    @GetMapping("/api/assessment/inspector/my-tasks")
    public InspectorTaskPaginationDto getMyTasks(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "5") int size,
            Principal principal) {
        return assessmentService.getMyTasks(principal.getName(), page, size);
    }
//submit assessment report by assessment id
    @PutMapping("/api/assessment/submit/{id}")
    public void submitAssessmentReport(
            @PathVariable Long id,
            @Valid @RequestBody SubmitAssessmentReportDto dto,
            Principal principal) {
        assessmentService.submitSurveyReport(id, dto, principal.getName());
    }


    // Get assessment by ID
    @GetMapping("/api/assessment/v1/{id}")
    public ClaimAssessmentResponseDto getById(@PathVariable Long id) {
        return assessmentService.getById(id);
    }

    // Get assessment by claim ID
    @GetMapping("/api/assessment/claim/v2/{claimId}")
    public ClaimAssessmentResponseDto getByClaimId(@PathVariable Long claimId) {
        return assessmentService.getByClaimId(claimId);
    }

    // Get assessments by inspector ID
    @GetMapping("/api/assessment/inspector/v3/{inspectorId}")
    public List<ClaimAssessmentResponseDto> getByInspectorId(@PathVariable Long inspectorId) {
        return assessmentService.getByInspectorId(inspectorId);
    }


    // Get all assessments
    @GetMapping("/api/assessment/all")
    public List<ClaimAssessmentResponseDto> getAll() {
        return assessmentService.getAll();
    }
}