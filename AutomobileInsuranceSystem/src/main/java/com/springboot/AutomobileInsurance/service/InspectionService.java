package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.SubmitInspectionReportDto;
import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDto;
import com.springboot.AutomobileInsurance.dto.response.InspectionResponseDtoV2;
import com.springboot.AutomobileInsurance.enums.InspectionResult;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.InspectionMapper;
import com.springboot.AutomobileInsurance.model.Inspection;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.repository.InspectionRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final ProposalRepository proposalRepository;
    private final Logger log = LoggerFactory.getLogger(InspectionService.class);

    //inspector submits inspection records
    @Transactional
    public void submitReport(Long proposalId, SubmitInspectionReportDto dto) {
        //Fetch Inspection entity from db by proposal id
        Inspection inspection = inspectionRepository.findByProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection record not found"));

        //Fetch proposal entity form inspection entity
        Proposal proposal = inspection.getProposal();

        //based on result of inspection, set recommended idv and update status of proposal
        if (dto.result() == InspectionResult.PASSED)
        {

            inspection.setRecommendedIdv(dto.recommendedIdv());
            log.info("Inspection PASSED for Proposal #{}. Recommended IDV: ₹{}", proposalId, dto.recommendedIdv());
        }
        else
        {
            proposal.setStatus(ProposalStatus.REJECTED);
            inspection.setRecommendedIdv(BigDecimal.ZERO);
            inspection.setApprovedIdv(BigDecimal.ZERO);
            log.info("Inspection FAILED for Proposal #{}. Reason/Notes: {}", proposalId, dto.inspectionNotes());
        }
        proposal.setStatus(ProposalStatus.INSPECTION_COMPLETED);

        //set inspection result in record
        inspection.setResult(dto.result());

        //set photos taken during inspection
        inspection.setInspectionPhotosUrl(dto.inspectionPhotosUrl());

        //set comments given by inspector
        inspection.setInspectionNotes(dto.inspectionNotes());

        //update status of inspection
        inspection.setStatus(InspectionStatus.COMPLETED);

        //save records in db
        inspectionRepository.save(inspection);
        proposalRepository.save(proposal);
        log.info("Inspection ID report saved ");
    }

    //get inspection report by inspector username (v1)
    public List<InspectionResponseDto> getReportsByInspectorUsername(String inspectorUsername, int page, int size) {
        log.debug("Fetching survey reports for inspector '{}' (page: {}, size: {})", inspectorUsername, page, size);
        Pageable pageable = PageRequest.of(page,size);
        return inspectionRepository.getReportsByInspectorUsername(inspectorUsername,pageable);

    }

    //get report by inspector username and status (v2)
    public List<InspectionResponseDto> getInspectionsByStatus(InspectionStatus status,String inspectorUsername) {

        log.debug("Fetching inspections for inspector '{}' with status '{}'", inspectorUsername, status);
        return inspectionRepository.getInspectionsByStatus(status,inspectorUsername);
    }


    //officer gets report by proposal id (v1)
    public InspectionResponseDtoV2 getByProposalId(Long proposalId) {
        log.debug("Fetching survey findings for Proposal #{}", proposalId);
        Inspection inspection = inspectionRepository.findByProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("No inspection record found for proposal id: " + proposalId));
        return InspectionMapper.convertEntityToDto(inspection);

    }

    //officer gets report by inspector id (v2)
    public List<InspectionResponseDtoV2> getInspectionsByInspector(Long inspectorId) {
        log.debug("Fetching all survey assignments for Inspector #{}", inspectorId);
        return inspectionRepository.findByInspectorId(inspectorId)
                .stream()
                .map(InspectionMapper::convertEntityToDto)
                .toList();
    }


}