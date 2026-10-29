package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.ClaimAssessmentRequestDto;
import com.springboot.AutomobileInsurance.dto.request.SubmitAssessmentReportDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimAssessmentResponseDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorTaskResponseDto;
import com.springboot.AutomobileInsurance.enums.AssessmentStatus;
import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.ClaimAssessmentMapper;
import com.springboot.AutomobileInsurance.model.Claim;
import com.springboot.AutomobileInsurance.model.ClaimAssessment;
import com.springboot.AutomobileInsurance.model.Inspector;
import com.springboot.AutomobileInsurance.repository.ClaimAssessmentRepository;
import com.springboot.AutomobileInsurance.repository.ClaimRepository;
import com.springboot.AutomobileInsurance.repository.InspectorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimAssessmentService {

    private final ClaimAssessmentRepository claimAssessmentRepository;
    private final ClaimRepository claimRepository;
    private final InspectorRepository inspectorRepository;


    /*
    public void createAssessment(ClaimAssessmentRequestDto request) {



        Claim claim = claimRepository.findById(request.claimId())
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        Inspector inspector = inspectorRepository.findById(request.inspectorId())
                .orElseThrow(() -> new ResourceNotFoundException("Inspector not found"));

        ClaimAssessment assessment = new ClaimAssessment();
        assessment.setClaim(claim);
        assessment.setInspector(inspector);
        assessment.setEstimatedCost(request.estimatedCost().setScale(2, RoundingMode.HALF_UP));
        assessment.setDamagePhotosUrl(request.damagePhotosUrl());
        assessment.setAssessmentNotes(request.assessmentNotes());
        assessment.setStatus(request.status() != null ? request.status() : AssessmentStatus.PENDING);

        claimAssessmentRepository.save(assessment);

    }

     */

    @Transactional
    public void updateAssessment(Long id, ClaimAssessmentRequestDto request) {
        ClaimAssessment assessment = claimAssessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim assessment not found"));
        boolean flag = false;

        if (request.estimatedCost() != null) {
            assessment.setEstimatedCost(request.estimatedCost().setScale(2, RoundingMode.HALF_UP));
            flag=true;
        }
        if (request.damagePhotosUrl() != null) {
            assessment.setDamagePhotosUrl(request.damagePhotosUrl());
            flag=true;
        }
        if (request.assessmentNotes() != null) {
            assessment.setAssessmentNotes(request.assessmentNotes());
            flag=true;
        }
        if (request.status() != null) {
            assessment.setStatus(request.status());
            flag=true;
        }

        if(!flag)
        {
            throw new InvalidCallException("Update Operation terminated");
        }

        claimAssessmentRepository.save(assessment);

    }


    public ClaimAssessmentResponseDto getById(Long id) {
        ClaimAssessment assessment = claimAssessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim assessment not found"));
        return ClaimAssessmentMapper.convertEntityToDto(assessment);
    }


    public ClaimAssessmentResponseDto getByClaimId(Long claimId) {
        ClaimAssessment assessment = claimAssessmentRepository.findByClaimId(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim assessment not found"));
        return ClaimAssessmentMapper.convertEntityToDto(assessment);
    }


    public List<ClaimAssessmentResponseDto> getByInspectorId(Long inspectorId) {
        return claimAssessmentRepository.findByInspectorId(inspectorId)
                .stream()
                .map(ClaimAssessmentMapper::convertEntityToDto)
                .toList();
    }


    public List<ClaimAssessmentResponseDto> getAll() {
        return claimAssessmentRepository.findAll()
                .stream()
                .map(ClaimAssessmentMapper::convertEntityToDto)
                .toList();
    }

    public List<InspectorTaskResponseDto> getMyTasks(String username) {
        return claimAssessmentRepository.findTasksByInspectorUsername(username);
    }

    //inspector submits the assessment report
    public void submitSurveyReport(Long assessmentId, SubmitAssessmentReportDto dto, String inspectorUsername) {
        // 1. Fetch Assessment record
        ClaimAssessment assessment = claimAssessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim assessment record not found"));

        // 2. Ownership Verification: Ensure logged-in inspector is assigned to this survey
        if (!assessment.getInspector().getUser().getUsername().equals(inspectorUsername)) {
            throw new InvalidCallException("Access denied: You are not assigned to this claim assessment.");
        }



        // 3. Update surveyor findings
        assessment.setEstimatedCost(dto.estimatedCost().setScale(2, RoundingMode.HALF_UP));
        assessment.setDamagePhotosUrl(dto.damagePhotosUrl());
        assessment.setAssessmentNotes(dto.assessmentNotes());
        assessment.setStatus(AssessmentStatus.COMPLETED);

        claimAssessmentRepository.save(assessment);

        // 4.update the status of the claim
        Claim claim = assessment.getClaim();

            claim.setStatus(ClaimStatus.UNDER_ASSESSMENT);
            claimRepository.save(claim);

    }
}