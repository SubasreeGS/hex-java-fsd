package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.ClaimFileRequestDto;
import com.springboot.AutomobileInsurance.dto.request.ClaimReviewRequestDto;
import com.springboot.AutomobileInsurance.dto.response.*;
import com.springboot.AutomobileInsurance.enums.AssessmentStatus;
import com.springboot.AutomobileInsurance.enums.ClaimStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.ClaimMapper;
import com.springboot.AutomobileInsurance.model.*;
import com.springboot.AutomobileInsurance.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final Logger log = LoggerFactory.getLogger(ClaimService.class);

    @Transactional
    public void fileClaim(ClaimFileRequestDto request) {
        //Fetch policy by its id
        Policy policy = policyRepository.findById(request.policyId())
                .orElseThrow(() -> {
                    log.warn("Claim registration failed: Policy #{} not found", request.policyId());
                    return new ResourceNotFoundException("Policy not found");
                });

        LocalDate incidentDate = request.incidentDate();

        // Verify incident occurred within policy active coverage dates
        if (incidentDate.isBefore(policy.getStartDate()) || incidentDate.isAfter(policy.getEndDate())) {
            log.warn("Claim filing rejected: Incident date {} is outside policy period ({} to {})",
                    incidentDate, policy.getStartDate(), policy.getEndDate());
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
        log.info("Claim registered successfully with Claim Number: '{}'", claim.getClaimNo());

    }

    public ClaimResponseDto getById(Long id) {
        log.debug("Fetching claim by id: {}", id);
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Claim #{} not found", id);
                    return new ResourceNotFoundException("Claim not found");
                });
        return ClaimMapper.convertEntityToDto(claim);
    }


    public ClaimResponseDto getByClaimNo(String claimNo) {
        log.debug("Fetching claim by claim number: {}", claimNo);
        Claim claim = claimRepository.findByClaimNo(claimNo)
                .orElseThrow(() -> {
                    log.warn("Claim number '{}' not found", claimNo);
                    return new ResourceNotFoundException("Claim not found");
                });
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
        log.debug("Fetching claims for customer: {}", username);
        return claimRepository.findClaimsByCustomerUsername(username);
    }

    @Transactional
    public void assignInspector(Long inspectorId, Long claimId, String officerUsername) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> {
                    log.warn("Claim assignment failed: Claim #{} not found", claimId);
                    return new ResourceNotFoundException("Claim not found");
                });


        Inspector inspector = inspectorRepository.findById(inspectorId)
                .orElseThrow(() -> {
                    log.warn("Claim assignment failed: Inspector #{} not found", inspectorId);
                    return new ResourceNotFoundException("Inspector not found");
                });

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
        log.info("Inspector assigned for the claim");

        // Update claim status so the officer knows it is under inspection
        claim.setStatus(ClaimStatus.UNDER_ASSESSMENT);
        claimRepository.save(claim);
    }

    public ClaimResponseV2PaginationDto getAllClaimsForOfficer(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ClaimResponseDtoV2> claimPage = claimRepository.findAllClaimsForOfficer(pageable);

        PaginationDto paginationDto = new PaginationDto(
                claimPage.getTotalPages(),
                claimPage.getTotalElements(),
                claimPage.getNumberOfElements()
        );

        return new ClaimResponseV2PaginationDto(claimPage.getContent(), paginationDto);
    }
    @Transactional
    public void reviewClaim(Long claimId, ClaimReviewRequestDto request) {
        log.info("officer is reviewing claim");
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));


        // Status-Specific Validation & Processing
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


//claim process settlement
@Transactional
    public void processSettlement(Long claimId) {
        log.info("Officer processing disbursement for Claim ID: {}", claimId);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim record not found"));
        

        // Generate settlement transaction reference reusing policy payment identifier
        String originalTxn = claim.getPolicy().getPayment().getTransactionRef();
        String settlementRef = "SETTLE-" + (int)((Math.random() * 900000) + 100000) + "-" + originalTxn;

        // Transition status to SETTLED and record metadata
        claim.setStatus(ClaimStatus.SETTLED);
        claim.setSettlementRef(settlementRef);
        claim.setSettledDate(LocalDateTime.now());

        claimRepository.save(claim);
    }

    public ClaimSettlementReceiptDto getSettlementReceipt(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim record not found"));

        if (claim.getStatus() != ClaimStatus.SETTLED) {
            throw new InvalidCallException("Receipt is only generated for SETTLED claims.");
        }
        log.info("Payout completed for Claim ID: {}",claimId);
        return claimRepository.getSettlementReceiptByClaimId(claimId);
    }
}