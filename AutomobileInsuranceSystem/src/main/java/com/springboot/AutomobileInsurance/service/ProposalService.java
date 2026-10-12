package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.request.ProposalSubmissionDto;
import com.springboot.AutomobileInsurance.dto.response.ProposalResponseDto;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.exception.InvalidCallException;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.mapper.ProposalMapper;
import com.springboot.AutomobileInsurance.model.Customer;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.model.RtoVehicle;
import com.springboot.AutomobileInsurance.repository.CustomerRepository;
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

    public ProposalResponseDto submitProposal(ProposalSubmissionDto dto) {
        // Step 1: Validate customer existence
        Customer customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.customerId()));

        // Step 2: Validate vehicle against RTO catalog
        RtoVehicle vehicle = rtoVehicleRepository.findByRegistrationNo(dto.registrationNo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found in RTO records: " + dto.registrationNo()));

        // Step 3: Prevent duplicate active proposals for the same vehicle
        List<ProposalStatus> terminalStatuses = List.of(ProposalStatus.POLICY_ISSUED, ProposalStatus.REJECTED);
        boolean hasActiveProposal = proposalRepository.existsByVehicleIdAndStatusNotIn(vehicle.getId(), terminalStatuses);
        if (hasActiveProposal) {
            throw new InvalidCallException("An active proposal already exists for vehicle: " + dto.registrationNo());
        }

        // Step 4: Map DTO to entity and initialize status
        Proposal proposal = ProposalMapper.convertDtoToEntity(dto, customer, vehicle);
        proposal.setStatus(ProposalStatus.PROPOSAL_SUBMITTED);

        // Step 5: Save entity
        Proposal savedProposal = proposalRepository.save(proposal);

        return ProposalMapper.convertEntityToDto(savedProposal);
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