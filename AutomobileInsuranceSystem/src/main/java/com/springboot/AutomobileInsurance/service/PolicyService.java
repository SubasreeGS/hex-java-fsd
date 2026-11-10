package com.springboot.AutomobileInsurance.service;

import com.springboot.AutomobileInsurance.dto.response.PaginationDto;
import com.springboot.AutomobileInsurance.dto.response.PolicyPaginationDto;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final ProposalRepository proposalRepository;
    private final QuoteRepository quoteRepository;
    private final Logger log = LoggerFactory.getLogger(PolicyService.class);

    //get policy by policy number
    public PolicyResponseDto getByPolicyNumber(String policyNumber) {
        log.debug("Fetching policy by policy number: {}", policyNumber);
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));
        return policyRepository.getByPolicyNumber(policyNumber);

    }

    //get by quote id
    public PolicyResponseDto getByQuoteId(Long quoteId) {
        log.debug("Fetching policy by quote ID: {}", quoteId);
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(()-> new ResourceNotFoundException("Quote not found"));

        return policyRepository.getByQuoteId(quoteId);

    }

    //get by proposal id
    public PolicyResponseDto getByProposalId(Long proposalId) {
        log.debug("Fetching policy by proposal ID: {}", proposalId);
        //validate the proposal id
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal id not found"));
        return policyRepository.getByProposalId(proposalId);
    }

    // get all proposals
    public List<PolicyResponseDto> getAll() {
        log.debug("Fetching all policies");
        return policyRepository.findAllPolicyDto();
    }


    public List<PolicyResponseDtoV2> getMyPolicyCards(String name) {
        log.debug("Fetching policy cards for customer: {}", name);
        return policyRepository.findCustomerPolicyByUsername(name);

    }

    public PolicyPaginationDto getAllPoliciesWithPagination(int page, int size) {
        int pageNo = Math.max(page, 0);
        int pageSize = (size < 1) ? 5 : size;
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        log.debug("Fetching paginated policies: page={}, size={}", pageNo, pageSize);

        Page<PolicyResponseDto> policyPage = policyRepository.findAllPoliciesPaginated(pageable);

        PaginationDto paginationDto = new PaginationDto(
                policyPage.getTotalPages(),
                policyPage.getTotalElements(),
                policyPage.getNumberOfElements()
        );

        return new PolicyPaginationDto(policyPage.getContent(), paginationDto);
    }
}