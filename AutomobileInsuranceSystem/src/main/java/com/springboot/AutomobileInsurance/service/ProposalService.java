package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.ProposalSubmissionDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalResponseDto;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.ProposalMapper;
import com.springboot.AutomobileInsurance.model.Customer;
import com.springboot.AutomobileInsurance.model.Inspection;
import com.springboot.AutomobileInsurance.model.Inspector;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.model.RtoVehicle;
import com.springboot.AutomobileInsurance.repository.CustomerRepository;
import com.springboot.AutomobileInsurance.repository.InspectionRepository;
import com.springboot.AutomobileInsurance.repository.InspectorRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import com.springboot.AutomobileInsurance.repository.RtoVehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final CustomerRepository customerRepository;
    private final RtoVehicleRepository rtoVehicleRepository;
    private final InspectorRepository inspectorRepository;
    private final InspectionRepository inspectionRepository;

    public ProposalResponseDto submitProposal(ProposalSubmissionDto dto) {
        if (dto.policyType() == PolicyType.THIRD_PARTY) {
            if (dto.isZeroDep() || dto.isEngineProtect() || dto.isRoadsideAssist()) {
                throw new InvalidCallException("Add-on covers cannot be selected with a Third-Party policy.");
            }
        }

        Customer customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.customerId()));

        RtoVehicle vehicle = rtoVehicleRepository.findByRegistrationNo(dto.registrationNo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found in RTO records: " + dto.registrationNo()));

        List<ProposalStatus> terminalStatuses = List.of(ProposalStatus.POLICY_ISSUED, ProposalStatus.REJECTED);
        boolean hasActiveProposal = proposalRepository.existsByVehicleIdAndStatusNotIn(vehicle.getId(), terminalStatuses);
        if (hasActiveProposal) {
            throw new InvalidCallException("An active proposal already exists for vehicle: " + dto.registrationNo());
        }

        Proposal proposal = ProposalMapper.convertDtoToEntity(dto, customer, vehicle);
        proposal.setStatus(ProposalStatus.PROPOSAL_SUBMITTED);

        Proposal savedProposal = proposalRepository.save(proposal);
        return ProposalMapper.convertEntityToDto(savedProposal);
    }

    public void assignInspector(Long inspectorId, Long proposalId) {
        // Step 1: Validate Inspector
        Inspector inspector = inspectorRepository.findById(inspectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspector not found with id: " + inspectorId));

        // Step 2: Validate Proposal
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + proposalId));

        // Step 3: Verify proposal status
        if (proposal.getStatus() != ProposalStatus.PROPOSAL_SUBMITTED && proposal.getStatus() != ProposalStatus.UNDER_REVIEW) {
            throw new InvalidCallException("Cannot assign inspector. Proposal is in state: " + proposal.getStatus());
        }

        // Step 4: Update proposal state
        proposal.setStatus(ProposalStatus.INSPECTOR_ASSIGNED);
        proposalRepository.save(proposal);

        // Step 5: Schedule inspection
        Inspection inspection = new Inspection();
        inspection.setProposal(proposal);
        inspection.setInspector(inspector);
        inspection.setStatus(InspectionStatus.SCHEDULED);

        // Step 6: save in db
        inspectionRepository.save(inspection);
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
}