package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDto;
import com.springboot.AutomobileInsurance.dto.response.PolicyResponseDtoV2;
import com.springboot.AutomobileInsurance.exception.ResourceNotFoundException;
import com.springboot.AutomobileInsurance.model.Policy;
import com.springboot.AutomobileInsurance.model.Proposal;
import com.springboot.AutomobileInsurance.model.Quote;
import com.springboot.AutomobileInsurance.repository.PolicyRepository;
import com.springboot.AutomobileInsurance.repository.ProposalRepository;
import com.springboot.AutomobileInsurance.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final ProposalRepository proposalRepository;
    private final QuoteRepository quoteRepository;

    //get policy by policy number
    public PolicyResponseDto getByPolicyNumber(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));
        return policyRepository.getByPolicyNumber(policyNumber);
    }

    //get by quote id
    public PolicyResponseDto getByQuoteId(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(()-> new ResourceNotFoundException("Quote not found"));

        return policyRepository.getByQuoteId(quoteId);
    }

    //get by proposal id
    public PolicyResponseDto getByProposalId(Long proposalId) {
        //validate the proposal id
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal id not found"));
        return policyRepository.getByProposalId(proposalId);
    }

    // get all proposals
    public List<PolicyResponseDto> getAll() {
        return policyRepository.findAllPolicyDto();
    }


    public List<PolicyResponseDtoV2> getMyPolicyCards(String name) {
        return policyRepository.findCustomerPolicyByUsername(name);
    }
}