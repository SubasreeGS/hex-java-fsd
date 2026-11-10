package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.config.GlobalExceptionHandler;
import com.springboot.AutomobileInsurance.dto.request.ClaimAssessmentRequestDto;
import com.springboot.AutomobileInsurance.dto.request.SubmitAssessmentReportDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimAssessmentResponseDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorTaskPaginationDto;
import com.springboot.AutomobileInsurance.dto.response.InspectorTaskResponseDto;
import com.springboot.AutomobileInsurance.dto.response.PaginationDto;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimAssessmentService {

    private final ClaimAssessmentRepository claimAssessmentRepository;
    private final ClaimRepository claimRepository;
    Logger log = LoggerFactory.getLogger(ClaimAssessmentService.class);
    private final InspectorRepository inspectorRepository;

    @Transactional
    public void updateAssessment(Long id, ClaimAssessmentRequestDto request) {
        ClaimAssessment assessment = claimAssessmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Assessment update failed: Assessment #{} not found", id);
                    return new ResourceNotFoundException("Claim assessment not found");
                });
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
            log.warn("Update assessment operation rejected: No valid fields provided for Assessment #{}", id);
            throw new InvalidCallException("Update Operation terminated");
        }

        claimAssessmentRepository.save(assessment);
        log.info("Claim Assessment #{} updated successfully", id);
    }


    public ClaimAssessmentResponseDto getById(Long id) {
        log.debug("Fetching claim assessment by id: {}", id);

        ClaimAssessment assessment = claimAssessmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Claim assessment #{} not found", id);
                    return new ResourceNotFoundException("Claim assessment not found");
                });
        return ClaimAssessmentMapper.convertEntityToDto(assessment);
    }


    public ClaimAssessmentResponseDto getByClaimId(Long claimId) {
        log.debug("Fetching claim assessment for Claim #{}", claimId);
        ClaimAssessment assessment = claimAssessmentRepository.findByClaimId(claimId)
                .orElseThrow(() -> {
                    log.warn("Claim assessment for Claim #{} not found", claimId);
                    return new ResourceNotFoundException("Claim assessment not found");
                });
        return ClaimAssessmentMapper.convertEntityToDto(assessment);
    }


    public List<ClaimAssessmentResponseDto> getByInspectorId(Long inspectorId) {
        log.debug("Fetching assessments for Inspector #{}", inspectorId);
        return claimAssessmentRepository.findByInspectorId(inspectorId)
                .stream()
                .map(ClaimAssessmentMapper::convertEntityToDto)
                .toList();
    }


    public List<ClaimAssessmentResponseDto> getAll() {

        log.debug("Fetching all claim assessments");
        return claimAssessmentRepository.findAll()
                .stream()
                .map(ClaimAssessmentMapper::convertEntityToDto)
                .toList();
    }

    public InspectorTaskPaginationDto getMyTasks(String username, int page, int size) {

        log.debug("Fetching task page {} (size {}) for surveyor: {}", page, size, username);
        Pageable pageable = PageRequest.of(page, size);
        Page<InspectorTaskResponseDto> taskPage = claimAssessmentRepository.findTasksByInspectorUsername(username, pageable);

        PaginationDto paginationDto = new PaginationDto(
                taskPage.getTotalPages(),
                taskPage.getTotalElements(),
                taskPage.getNumberOfElements()
        );

        return new InspectorTaskPaginationDto(taskPage.getContent(), paginationDto);
    }

    //inspector submits the assessment report
    @Transactional
    public void submitSurveyReport(Long assessmentId, SubmitAssessmentReportDto dto, String inspectorUsername) {
        // 1. Fetch Assessment record
        ClaimAssessment assessment = claimAssessmentRepository.findById(assessmentId)
                .orElseThrow(() -> {
                    log.warn("Survey report submission failed: Assessment #{} not found", assessmentId);
                    return new ResourceNotFoundException("Claim assessment record not found");
                });

        // 2. Ownership Verification: Ensure logged-in inspector is assigned to this survey
        if (!assessment.getInspector().getUser().getUsername().equals(inspectorUsername)) {
            log.warn("Survey report submission failed: Inspector #{} is not assigned to Assessment #{}", inspectorUsername, assessmentId);
            throw new InvalidCallException("Access denied: You are not assigned to this claim assessment.");
        }

        // 3. Update surveyor findings
        assessment.setEstimatedCost(dto.estimatedCost().setScale(2, RoundingMode.HALF_UP));
        assessment.setDamagePhotosUrl(dto.damagePhotosUrl());
        assessment.setAssessmentNotes(dto.assessmentNotes());
        assessment.setStatus(AssessmentStatus.COMPLETED);

        claimAssessmentRepository.save(assessment);

        log.debug("Survey report submitted for Assessment #{}", assessmentId);
        // 4.update the status of the claim
        Claim claim = assessment.getClaim();

            claim.setStatus(ClaimStatus.UNDER_ASSESSMENT);
            claimRepository.save(claim);

    }


}