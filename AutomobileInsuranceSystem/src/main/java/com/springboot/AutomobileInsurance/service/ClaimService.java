package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.ClaimFileRequestDto;
import com.springboot.AutomobileInsurance.dto.request.ClaimReviewRequestDto;
import com.springboot.AutomobileInsurance.dto.response.ClaimResponseDto;
import com.springboot.AutomobileInsurance.enums.AssessmentStatus;
import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.ClaimMapper;
import com.springboot.AutomobileInsurance.model.*;
import com.springboot.AutomobileInsurance.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final OfficerRepository officerRepository;
    private final InspectorRepository inspectorRepository;
    private final ClaimAssessmentRepository claimAssessmentRepository;

    @Transactional
    public void fileClaim(ClaimFileRequestDto request) {
        //Fetch policy by its id
        Policy policy = policyRepository.findById(request.policyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

        LocalDate incidentDate = request.incidentDate();

        // Verify incident occurred within policy active coverage dates
        if (incidentDate.isBefore(policy.getStartDate()) || incidentDate.isAfter(policy.getEndDate())) {
            throw new InvalidCallException("Incident date must fall within Policy coverage period");

        }

        // Build and save claim
        Claim claim = new Claim();
        claim.setClaimNo(generateUniqueClaimNumber());
        claim.setPolicy(policy);
        claim.setIncidentDate(incidentDate);
        claim.setIncidentDescription(request.incidentDescription());
        claim.setClaimedAmount(request.claimedAmount().setScale(2, RoundingMode.HALF_UP));
        claim.setApprovedAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        claim.setStatus(ClaimStatus.FILED);

         claimRepository.save(claim);

    }
/*
    @Transactional
    public void reviewClaim(Long claimId, ClaimReviewRequestDto request) {

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));


        Officer officer = officerRepository.findById(request.officerId())
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));

        claim.setOfficer(officer);

        if (request.status() == ClaimStatus.APPROVED)
        {
            claim.setApprovedAmount(request.approvedAmount().setScale(2, RoundingMode.HALF_UP));
            claim.setStatus(ClaimStatus.APPROVED);
        }
        else if (request.status() == ClaimStatus.REJECTED)
        {
            claim.setApprovedAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            claim.setStatus(ClaimStatus.REJECTED);
        }
        else
        {
            claim.setStatus(request.status());
        }

        claimRepository.save(claim);

    }
    
 */


    public ClaimResponseDto getById(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));
        return ClaimMapper.convertEntityToDto(claim);
    }


    public ClaimResponseDto getByClaimNo(String claimNo) {
        Claim claim = claimRepository.findByClaimNo(claimNo)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));
        return ClaimMapper.convertEntityToDto(claim);
    }


    public List<ClaimResponseDto> getByPolicyId(Long policyId) {
        return claimRepository.findByPolicyId(policyId)
                .stream()
                .map(ClaimMapper::convertEntityToDto)
                .toList();
    }

    @Transactional
    public List<ClaimResponseDto> getAll() {
        return claimRepository.findAll()
                .stream()
                .map(ClaimMapper::convertEntityToDto)
                .toList();
    }

    private String generateUniqueClaimNumber() {
        String claimNo;
        do {
            int randomNum=(int)((Math.random()*10000));
            claimNo = "CLM-" + Year.now().getValue() + "-" + randomNum;
        } while (claimRepository.existsByClaimNo(claimNo));
        return claimNo;
    }

    public List<ClaimResponseDto> getClaimsByCustomerUsername(String username) {
        return claimRepository.findClaimsByCustomerUsername(username);
    }

    public void assignInspector(Long inspectorId, Long claimId, String officerUsername) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        Inspector inspector = inspectorRepository.findById(inspectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspector not found"));

        // link the reviewing officer to the claim
        Officer officer = officerRepository.findByOfficerUsername(officerUsername);

        claim.setOfficer(officer);

        // Create an initial PENDING ClaimAssessment record for this inspector
        ClaimAssessment assessment = new ClaimAssessment();
        assessment.setClaim(claim);
        assessment.setInspector(inspector);
        assessment.setStatus(AssessmentStatus.PENDING);
        assessment.setEstimatedCost(BigDecimal.ZERO);

        claimAssessmentRepository.save(assessment);

        // Update claim status so the officer knows it is under inspection
        claim.setStatus(ClaimStatus.UNDER_ASSESSMENT);
        claimRepository.save(claim);
    }

    public void reviewClaim(Long claimId, ClaimReviewRequestDto request, String officerUsername) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        // 1. Verify Officer Assignment
        if ( !claim.getOfficer().getUser().getUsername().equals(officerUsername)) {
            throw new InvalidCallException("Unauthorized: You are not the assigned officer for this claim.");
        }


        // 3. Status-Specific Validation & Processing
        if (request.status() == ClaimStatus.APPROVED)
        {
            claim.setApprovedAmount(request.approvedAmount().setScale(2, RoundingMode.HALF_UP));
            claim.setStatus(ClaimStatus.APPROVED);

        }

        else if (request.status() == ClaimStatus.REJECTED) {
            claim.setApprovedAmount(BigDecimal.ZERO);
            claim.setStatus(ClaimStatus.REJECTED);
        }

        claimRepository.save(claim);
    }

}