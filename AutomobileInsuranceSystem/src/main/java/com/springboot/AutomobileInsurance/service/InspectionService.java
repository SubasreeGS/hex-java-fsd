package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.SubmitInspectionReportDto;
import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDto;
import com.springboot.AutomobileInsurance.enums.InspectionResult;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.InspectionMapper;
import com.springboot.AutomobileInsurance.model.Inspection;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.repository.InspectionRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final ProposalRepository proposalRepository;

    public InspectionResponseDto submitReport(Long proposalId, SubmitInspectionReportDto dto) {
        // Step 1: Fetch Inspection by proposal id
        Inspection inspection = inspectionRepository.findByProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("No inspection record found for proposal id: " + proposalId));

        // Step 2: Validate current Inspection status
        if (inspection.getStatus() == InspectionStatus.COMPLETED || inspection.getStatus() == InspectionStatus.CANCELLED) {
            throw new InvalidCallException("Inspection report cannot be submitted. Status is: " + inspection.getStatus());
        }

        Proposal proposal = inspection.getProposal();

        // Step 3: Validate rules for PASSED vs FAILED
        if (dto.result() == InspectionResult.PASSED) {
            if (dto.recommendedIdv() == null || dto.recommendedIdv().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidCallException("Recommended IDV must be greater than zero when inspection passes.");
            }
            proposal.setStatus(ProposalStatus.INSPECTION_COMPLETED);
            inspection.setRecommendedIdv(dto.recommendedIdv());
        } else {
            // If failed, proposal is rejected and no IDV applies
            proposal.setStatus(ProposalStatus.REJECTED);
            inspection.setRecommendedIdv(BigDecimal.ZERO);
        }

        // Step 4: Populate inspection details
        inspection.setResult(dto.result());
        inspection.setInspectionPhotosUrl(dto.inspectionPhotosUrl());
        inspection.setInspectionNotes(dto.inspectionNotes());
        inspection.setStatus(InspectionStatus.COMPLETED);

        // Step 5: Persist updates
        proposalRepository.save(proposal);
        Inspection savedInspection = inspectionRepository.save(inspection);

        return InspectionMapper.convertEntityToDto(savedInspection);
    }

    public InspectionResponseDto getByProposalId(Long proposalId) {
        Inspection inspection = inspectionRepository.findByProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("No inspection record found for proposal id: " + proposalId));
        return InspectionMapper.convertEntityToDto(inspection);
    }

    public List<InspectionResponseDto> getInspectionsByInspector(Long inspectorId) {
        return inspectionRepository.findByInspectorId(inspectorId)
                .stream()
                .map(InspectionMapper::convertEntityToDto)
                .toList();
    }
}