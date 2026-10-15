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
        Inspection inspection = inspectionRepository.findByProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("No inspection record found for proposal id: " + proposalId));

        if (inspection.getStatus() == InspectionStatus.COMPLETED || inspection.getStatus() == InspectionStatus.CANCELLED) {
            throw new InvalidCallException("Inspection report cannot be submitted. Status is: " + inspection.getStatus());
        }

        Proposal proposal = inspection.getProposal();

        if (dto.result() == InspectionResult.PASSED) {
            if (dto.recommendedIdv() == null || dto.recommendedIdv().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidCallException("Recommended IDV must be greater than zero when inspection passes.");
            }
            proposal.setStatus(ProposalStatus.INSPECTION_COMPLETED);
            inspection.setRecommendedIdv(dto.recommendedIdv());
        } else {
            proposal.setStatus(ProposalStatus.REJECTED);
            inspection.setRecommendedIdv(BigDecimal.ZERO);
            inspection.setApprovedIdv(BigDecimal.ZERO);
        }

        inspection.setResult(dto.result());
        inspection.setInspectionPhotosUrl(dto.inspectionPhotosUrl());
        inspection.setInspectionNotes(dto.inspectionNotes());
        inspection.setStatus(InspectionStatus.COMPLETED);

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
    public List<InspectionResponseDto> getInspectionsByStatus(InspectionStatus status) {
        return inspectionRepository.findByStatus(status)
                .stream()
                .map(InspectionMapper::convertEntityToDto)
                .toList();
    }
}