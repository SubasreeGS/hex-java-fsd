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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final ProposalRepository proposalRepository;

    //inspector submits inspection records
    public void submitReport(Long proposalId, SubmitInspectionReportDto dto) {
        //Fetch Inspection entity from db by proposal id
        Inspection inspection = inspectionRepository.findByProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection record not found"));

        //Fetch proposal entity form inspection entity
        Proposal proposal = inspection.getProposal();

        //based on result of inspection, set recommended idv and update status of proposal
        if (dto.result() == InspectionResult.PASSED)
        {
            proposal.setStatus(ProposalStatus.INSPECTION_COMPLETED);
            inspection.setRecommendedIdv(dto.recommendedIdv());
        }
        else
        {
            proposal.setStatus(ProposalStatus.REJECTED);
            inspection.setRecommendedIdv(BigDecimal.ZERO);
            inspection.setApprovedIdv(BigDecimal.ZERO);
        }

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

    }

    //get inspection report by inspector username (v1)
    public List<InspectionResponseDto> getReportsByInspectorUsername(String inspectorUsername, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        return inspectionRepository.getReportsByInspectorUsername(inspectorUsername,pageable);


    }

    //get report by inspector username and status (v2)
    public List<InspectionResponseDto> getInspectionsByStatus(InspectionStatus status,String inspectorUsername) {
        return inspectionRepository.getInspectionsByStatus(status,inspectorUsername);
    }


    //officer gets report by proposal id (v1)
    public InspectionResponseDtoV2 getByProposalId(Long proposalId) {
        Inspection inspection = inspectionRepository.findByProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("No inspection record found for proposal id: " + proposalId));
        return InspectionMapper.convertEntityToDto(inspection);

    }

    //officer gets report by inspector id (v2)
    public List<InspectionResponseDtoV2> getInspectionsByInspector(Long inspectorId) {
        return inspectionRepository.findByInspectorId(inspectorId)
                .stream()
                .map(InspectionMapper::convertEntityToDto)
                .toList();
    }


}