package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.ApproveIdvDto;
import com.springboot.AutomobileInsurance.dto.request.ProposalSubmissionDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalResponseDto;
import com.springboot.AutomobileInsurance.enums.InspectionResult;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.ProposalMapper;
import com.springboot.AutomobileInsurance.model.*;
import com.springboot.AutomobileInsurance.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final CustomerRepository customerRepository;
    private final RtoVehicleRepository rtoVehicleRepository;
    private final InspectorRepository inspectorRepository;
    private final InspectionRepository inspectionRepository;
    private final OfficerRepository officerRepository;

    public void submitProposal(ProposalSubmissionDto dto,String customerUsername) {

        //Fetch customer object
        Customer customer = customerRepository.getByCustomerUsername(customerUsername);

        //Fetch vehicle object
        RtoVehicle vehicle = rtoVehicleRepository.findByRegistrationNo(dto.registrationNo())
                .orElseThrow(() -> new ResourceNotFoundException("Registration Number not found"));

        //convert dto to entity
        Proposal proposal = ProposalMapper.convertDtoToEntity(dto);

        //add customer to proposal
        proposal.setCustomer(customer);

        //add vehicle to proposal
        proposal.setVehicle(vehicle);

        //set status as submitted
        proposal.setStatus(ProposalStatus.PROPOSAL_SUBMITTED);

        //save proposal in db
         proposalRepository.save(proposal);

    }

    public void assignInspector(Long inspectorId, Long proposalId,String officerUsername) {
        //fetch officer entity
        Officer officer = officerRepository.findByOfficerUsername(officerUsername);

        //Fetch Inspector from DB using inspectorId
        Inspector inspector = inspectorRepository.findById(inspectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspector not found"));


        //check does this officer have authority to assign this inspector
        if (! (inspector.getOfficer().getUser().getUsername().equals(officerUsername))) {
            throw new InvalidCallException("Operation not allowed! contact admin");
        }

        //Fetch proposal from DB using proposalId
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));

        //Create inspection entity
        Inspection inspection = new Inspection();

        //attach proposal to inspection
        inspection.setProposal(proposal);

        //assign inspector for this proposal
        inspection.setInspector(inspector);

        //set status of inspection
        inspection.setStatus(InspectionStatus.SCHEDULED);

        //save inspection in db
        inspectionRepository.save(inspection);

        proposal.setOfficer(officer);
        //set status of proposal as inspector assigned
        proposal.setStatus(ProposalStatus.INSPECTOR_ASSIGNED);

        //save proposal to db
        proposalRepository.save(proposal);
    }

    public void approveIdv(Long proposalId, ApproveIdvDto dto,String officerUsername) {
        //Fetch inspection record by proposal id
        Inspection inspection = inspectionRepository.findByProposalId(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection record not found"));

        //verify whether the officer has permission to approve this idv
        if(! (inspection.getInspector().getOfficer().getUser().getUsername().equals(officerUsername))){
            throw new InvalidCallException("Operation not allowed! contact admin");
        }

        //check inspection status before approving
        if (inspection.getResult() != InspectionResult.PASSED) {
            throw new InvalidCallException("Cannot approve IDV for a failed inspection.");
        }
        //approve idv
        inspection.setApprovedIdv(dto.approvedIdv());
        //save inspection record in db
        inspectionRepository.save(inspection);

        //Fetch proposal entity from inspection
        Proposal proposal = inspection.getProposal();

        //update proposal status
        proposal.setStatus(ProposalStatus.IDV_APPROVED);
        //save proposal in db
        proposalRepository.save(proposal);
    }

    public ProposalResponseDto getById(long id) {
        Optional<Proposal> optional = proposalRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Invalid proposal id: " + id);
        }
        return ProposalMapper.convertEntityToDto(optional.get());
    }

    public List<ProposalResponseDto> getAll() {
        return proposalRepository.findAll()
                .stream()
                .map(ProposalMapper::convertEntityToDto)
                .toList();
    }

    public List<ProposalResponseDto> getByCustomerId(long customerId) {
        return proposalRepository.findByCustomerId(customerId)
                .stream()
                .map(ProposalMapper::convertEntityToDto)
                .toList();
    }

    public List<ProposalResponseDto> getByStatus(ProposalStatus status) {
        return proposalRepository.findByStatus(status)
                .stream()
                .map(ProposalMapper::convertEntityToDto)
                .toList();
    }

    public List<ProposalResponseDto> getByOfficerId(long officerId) {
        // Step 1: Query all proposals assigned to the specific officer
        return proposalRepository.findByOfficerId(officerId)
                .stream()
                .map(ProposalMapper::convertEntityToDto)
                .toList();
    }
}